package ru.atomofiron.regextool.iss.service

import android.preference.PreferenceManager
import app.atomofiron.common.util.KObservable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.atomofiron.regextool.App
import ru.atomofiron.regextool.iss.service.model.Change
import ru.atomofiron.regextool.iss.service.model.Change.*
import ru.atomofiron.regextool.iss.service.model.Change.Nothing
import ru.atomofiron.regextool.iss.service.model.MutableXFile
import ru.atomofiron.regextool.iss.service.model.XFile
import ru.atomofiron.regextool.iss.store.SettingsStore
import ru.atomofiron.regextool.log2
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Shell
import java.io.File
import java.io.FileOutputStream

class ExplorerService {
    private val sp = PreferenceManager.getDefaultSharedPreferences(App.context)

    private val mutex = Mutex()
    private val files: MutableList<MutableXFile> = ArrayList()
    private var currentOpenedDir: MutableXFile? = null
        set(value) {
            field = value
            notifyCurrent(value)
        }

    val store = KObservable<List<XFile>>(files)
    val updates = KObservable<Change>(Nothing, single = true)

    private val useSu: Boolean get() = SettingsStore.useSu.value

    init {
        GlobalScope.launch(Dispatchers.IO) {
            mutex.withLock {
                copyToybox()
            }
        }
    }

    /*
    open dir
    close dir
    reopen dir (close opened child)
     */

    suspend fun addRoots(vararg path: String) {
        val roots = path.map { MutableXFile.byPath(it) }

        mutex.withLock {
            files.addAll(roots)
        }
        roots.forEach { updateClosedDir(it) }
    }

    fun invalidateDir(f: XFile) {
        val dir = findFile(f) ?: return
        invalidateDir(dir)
    }

    private fun invalidateDir(dir: MutableXFile) {
        log2("invalidateDir $dir")
        dir.invalidateCache()
    }

    fun persistState() {
        sp.edit().putString(Const.PREF_CURRENT_DIR, currentOpenedDir?.completedPath).apply()
    }

    private fun findFile(f: XFile): MutableXFile? = findFile(f.completedPath, f.root)

    private fun findFile(completedPath: String, root: Int): MutableXFile? {
        val files = files
        var i = 0
        // ConcurrentModificationException
        while (i < files.size) {
            val file = files[i++]
            if (file.root == root && file.completedPath == completedPath) {
                return file
            }
        }
        return null
    }

    private fun findOpenedDirInParentOf(dir: MutableXFile): MutableXFile? {
        return findOpenedDirIn(dir.completedParentPath, dir.root)
    }

    private fun findOpenedDirIn(dir: MutableXFile): MutableXFile? {
        return findOpenedDirIn(dir.completedPath, dir.root)
    }

    private fun findOpenedDirIn(completedParentPath: String, root: Int): MutableXFile? {
        val files = files
        var i = 0
        // ConcurrentModificationException
        while (i < files.size) {
            val file = files[i++]
            if (file.isOpened &&
                file.root == root &&
                file.completedParentPath == completedParentPath &&
                !file.isRoot) {
                return file
            }
        }
        return null
    }

    private fun findOpenedAnotherRoot(root: Int): MutableXFile? {
        val files = files
        var i = 0
        // ConcurrentModificationException
        while (i < files.size) {
            val file = files[i++]
            if (file.isRoot && file.isOpened && file.root != root) {
                return file
            }
        }
        return null
    }

    suspend fun updateFile(f: XFile) {
        val file = findFile(f) ?: return
        log2("updateFile $file")
        when {
            file.isOpened && file == currentOpenedDir -> updateCurrentDir(file)
            file.isOpened -> Unit
            file.isDirectory -> updateClosedDir(file)
            else -> return updateTheFile(file)
        }
    }

    suspend fun openDir(f: XFile) {
        val dir = findFile(f) ?: return
        if (!dir.isDirectory) {
            log2("openDir return $dir")
            return
        }
        if (!dir.isCached) {
            return updateClosedDir(dir)
        }
        when {
            dir == currentOpenedDir -> closeDir(dir)
            dir.isOpened -> reopenDir(dir)
            else -> openDir(dir)
        }
    }

    private suspend fun reopenDir(dir: MutableXFile) {
        log2("reopenDir $dir")
        if (dir == currentOpenedDir) {
            closeDir(dir)
            return
        }
        val childDir = findOpenedDirIn(dir)
        if (childDir != null) {
            log2("reopenDir anotherDir != null $childDir")
            childDir.close()
            childDir.clearChildren()
            currentOpenedDir = dir
            invalidateDir(dir)

            removeAllChildren(childDir)
            notifyUpdate(childDir)
            updateCurrentDir(dir)
        }
    }

    private suspend fun openDir(dir: MutableXFile) {
        require(dir.isDirectory) { IllegalArgumentException("Is not a directory! $dir") }
        log2("openDir $dir")
        val anotherDir = findOpenedDirInParentOf(dir)
        if (anotherDir != null) {
            log2("openDir $dir anotherDir == $anotherDir")
            anotherDir.close()
            anotherDir.clearChildren()
            removeAllChildren(anotherDir)
            notifyUpdate(anotherDir)
        } else {
            log2("anotherDir == null $dir")
        }

        var anotherRoot = findOpenedAnotherRoot(dir.root)
        while (anotherRoot != null) {
            anotherRoot.close()
            anotherRoot.clearChildren()
            removeAllChildren(anotherRoot)
            notifyUpdate(anotherRoot)
            anotherRoot = findOpenedAnotherRoot(dir.root)
        }

        val dirFiles = mutex.withLock {
            dir.open()
            currentOpenedDir = dir
            invalidateDir(dir)

            val dirFiles = dir.files!!
            if (dirFiles.isNotEmpty()) {
                val index = files.indexOf(dir)
                files.addAll(index.inc(), dirFiles)
            }
            dirFiles
        }
        notifyUpdate(dir)
        if (dirFiles.isNotEmpty()) {
            notifyInsertRange(dir, dirFiles)
        }
        updateCurrentDir(dir)
    }

    private suspend fun closeDir(f: XFile) {
        val dir = findFile(f) ?: return
        log2("closeDir $dir")
        val parent = if (dir.isRoot) null else findFile(dir.completedParentPath, dir.root)
        mutex.withLock {
            dir.close()
            currentOpenedDir = parent
        }

        dir.clearChildren()
        removeAllChildren(dir)
        notifyUpdate(dir)

        if (parent != null) {
            parent.invalidateCache()
            updateCurrentDir(parent)
        }
    }

    private suspend fun updateTheFile(file: MutableXFile) {
        log2("updateTheFile $file")
        when {
            file.updateCache(useSu) == null -> notifyUpdate(file)
            !file.exists -> dropEntity(file)
        }
    }

    private suspend fun updateCurrentDir(dir: MutableXFile) {
        if (dir != currentOpenedDir) {
            log2("updateCurrentDir dir != currentOpenedDir $dir")
            return
        }
        log2("updateCurrentDir $dir")
        val dirFiles = dir.files
        val error = dir.updateCache(useSu)
        if (error != null) {
            if (!dir.exists) {
                closeDir(dir)
            }
            log2("updateCurrentDir: $error")
            return
        }
        val newFiles = dir.files!!

        mutex.withLock {
            if (!dir.isCached || !files.contains(dir)) {
                log2("updateCurrentDir return $dir")
                return
            }
            if (!dir.isOpened) {
                log2("updateCurrentDir !isOpened $dir")
                return notifyUpdate(dir)
            }
            if (dir != currentOpenedDir) {
                log2("updateCurrentDir !isCurrentOpenedDir $dir")
                return
            }
            if (dirFiles != null) {
                files.removeAll(dirFiles)
            }

            if (newFiles.isNotEmpty()) {
                val index = files.indexOf(dir)
                files.addAll(index.inc(), newFiles)
            }
            notifyUpdate(dir)
            notifyCurrentDirChanges(dir, dirFiles, newFiles)
        }
    }

    private fun notifyCurrentDirChanges(dir: MutableXFile, dirFiles: List<XFile>?, newFiles: List<MutableXFile>) {
        val wasNullOrEmpty = dirFiles.isNullOrEmpty()
        val nowIsEmpty = newFiles.isEmpty()
        when {
            wasNullOrEmpty && !nowIsEmpty -> notifyInsertRange(dir, newFiles)
            !wasNullOrEmpty && nowIsEmpty -> notifyRemoveRange(dirFiles!!)
            !wasNullOrEmpty && !nowIsEmpty -> {
                dirFiles!!
                val news = newFiles.iterator()
                val olds = dirFiles.iterator()
                var previous: XFile = dir

                while (olds.hasNext()) {
                    val next = olds.next()
                    if (!newFiles.contains(next)) {
                        notifyRemove(next)
                    }
                }
                while (news.hasNext()) {
                    val next = news.next()
                    if (!dirFiles.contains(next)) {
                        notifyInsert(previous, next)
                    }
                    previous = next
                }
            }
        }
    }

    private suspend fun updateClosedDir(dir: MutableXFile) {
        require(dir.isDirectory) { IllegalArgumentException("Is not a directory! $dir") }
        if (dir.isOpened) {
            log2("updateClosedDir return $dir")
            return
        }
        log2("updateClosedDir $dir")
        val error = dir.updateCache(useSu)

        when {
            !dir.exists -> dropEntity(dir)
            error != null -> log2("updateClosedDir error != null ${dir.completedPath}\n$error")
            else -> {
                notifyUpdate(dir)
                /*
                нельзя не уведомлять, если файлы не изменились, потому что:
                1 у дочерних папок isCached=true, updateCache(), isCaching=true
                2 родительская папка закрывается, у дочерних папок clear(), files=null, isCached=false
                3 родительская папка открывается
                4 дочерние папки кешироваться не начинают, потому что isCaching=true
                5 список отображает папки как isCached=false
                6 по окончании кеширования может быть oldFiles==newFiles
                 */
            }
        }
    }

    private suspend fun removeAllChildren(dir: XFile) {
        val path = dir.completedPath
        val root = dir.root
        log2("removeAllChildren $path")
        val removed = mutex.withLock {
            val removed = ArrayList<XFile>()
            val each = files.iterator()
            loop@ while (each.hasNext()) {
                val next = each.next()
                when {
                    next.root == root && next.completedParentPath.startsWith(path) -> {
                        each.remove()
                        removed.add(next)
                    }
                    removed.isNotEmpty() -> break@loop
                }
            }
            removed
        }
        when (removed.isEmpty()) {
            true -> log2("removeAllChildren not found $path")
            false -> notifyRemoveRange(removed)
        }
    }

    private suspend fun dropEntity(entity: MutableXFile) {
        log2("dropEntity $entity")
        entity.clear()
        mutex.withLock {
            files.remove(entity)
        }
        notifyRemove(entity)
    }

    private fun copyToybox() {
        val pathToybox = App.pathToybox
        val toybox = File(pathToybox)
        toybox.deleteRecursively()
        toybox.parentFile.mkdirs()
        val input = App.context.assets.open("toybox/toybox64")
        val bytes = input.readBytes()
        input.close()
        val output = FileOutputStream(toybox)
        output.write(bytes)
        output.close()
        Shell.exec(Shell.NATIVE_CHMOD_X.format(pathToybox))
    }

    private fun notifyCurrent(file: XFile?) {
        log2("notifyCurrent $file")
        updates.setAndNotify(Current(file))
    }

    private fun notifyUpdate(file: XFile) {
        log2("notifyUpdate $file")
        updates.setAndNotify(Update(file))
    }

    private fun notifyRemove(file: XFile) {
        log2("notifyRemove $file")
        updates.setAndNotify(Remove(file))
    }

    private fun notifyInsert(previous: XFile, file: XFile) {
        log2("notifyInsert $file after $previous")
        updates.setAndNotify(Insert(previous, file))
    }

    private fun notifyRemoveRange(files: List<XFile>) {
        log2("notifyRemoveRange ${files.size}")
        updates.setAndNotify(RemoveRange(files))
    }

    private fun notifyInsertRange(previous: XFile, files: List<XFile>) {
        log2("notifyInsert ${files.size} after $previous")
        updates.setAndNotify(InsertRange(previous, files))
    }

    private suspend fun notifyFiles() {
        log2("notifyFiles")
        store.setAndNotify(files)
    }
}