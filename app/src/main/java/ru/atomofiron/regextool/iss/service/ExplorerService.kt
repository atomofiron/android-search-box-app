package ru.atomofiron.regextool.iss.service

import android.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.atomofiron.regextool.App
import ru.atomofiron.regextool.common.util.KObservable
import ru.atomofiron.regextool.iss.service.model.Change
import ru.atomofiron.regextool.iss.service.model.Change.*
import ru.atomofiron.regextool.iss.service.model.Change.Nothing
import ru.atomofiron.regextool.iss.service.model.MutableXFile
import ru.atomofiron.regextool.iss.service.model.XFile
import ru.atomofiron.regextool.iss.store.SettingsStore
import ru.atomofiron.regextool.log2
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Const.ROOT
import ru.atomofiron.regextool.utils.Shell
import java.io.File
import java.io.FileOutputStream

class ExplorerService {
    private val sp = PreferenceManager.getDefaultSharedPreferences(App.context)

    private val mutex = Mutex()
    private val files: MutableList<MutableXFile> = ArrayList()
    private lateinit var root: MutableXFile
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

    suspend fun setRoot(path: String) {
        root = MutableXFile.byPath(path)

        mutex.withLock {
            files.clear()
            files.add(root)
        }
        updateClosedDir(root)
        notifyFiles()
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

    private fun findFile(f: XFile): MutableXFile? = findFile(f.completedPath)

    private fun findFile(completedPath: String): MutableXFile? {
        val files = files
        var i = 0
        // ConcurrentModificationException
        while (i < files.size) {
            val file = files[i++]
            if (file.completedPath == completedPath) {
                return file
            }
        }
        return null
    }

    private fun findOpenedDirIn(completedParentPath: String): MutableXFile? {
        val files = files
        var i = 0
        // ConcurrentModificationException
        while (i < files.size) {
            val file = files[i++]
            if (file.isOpened && file.completedParentPath == completedParentPath && file.completedPath != ROOT) {
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
        val anotherDir = findOpenedDirIn(dir.completedPath)
        if (anotherDir != null) {
            log2("reopenDir anotherDir != null $anotherDir")
            anotherDir.close()
            anotherDir.clearChildren()
            currentOpenedDir = dir
            invalidateDir(dir)

            removeAllChildren(anotherDir)
            notifyUpdate(anotherDir)
            updateCurrentDir(dir)
        } else {
            closeDir(dir)
        }
    }

    private suspend fun openDir(dir: MutableXFile) {
        require(dir.isDirectory) { IllegalArgumentException("Is not a directory! $dir") }
        log2("openDir $dir")
        val anotherDir = findOpenedDirIn(dir.completedParentPath)
        if (anotherDir != null) {
            log2("openDir $dir anotherDir == $anotherDir")
            anotherDir.close()
            anotherDir.clearChildren()
            removeAllChildren(anotherDir)
            notifyUpdate(anotherDir)
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
        val parent = if (dir.completedPath == ROOT) null else findFile(dir.completedParentPath)
        mutex.withLock {
            dir.close()
            currentOpenedDir = parent
        }

        notifyUpdate(dir)
        dir.clearChildren()
        removeAllChildren(dir)
    }

    private suspend fun updateTheFile(file: MutableXFile) {
        log2("updateTheFile $file")
        when {
            file.updateCache() == null -> notifyUpdate(file)
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
                dropOpenedDir(dir)
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
                todo перепроверить
                 */
            }
        }
    }

    private suspend fun removeAllChildren(dir: XFile) = removeAllChildren(dir.completedPath)

    private suspend fun removeAllChildren(path: String) {
        log2("removeAllChildren $path")
        val removed = mutex.withLock {
            val removed = ArrayList<XFile>()
            val each = files.iterator()
            loop@ while (each.hasNext()) {
                val next = each.next()
                when {
                    next.completedParentPath.startsWith(path) -> {
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

    private suspend fun dropOpenedDir(d: MutableXFile) {
        var dirToDrop: XFile
        var targetDir = d
        do {
            dirToDrop = targetDir
            targetDir = findFile(targetDir.completedParentPath)!!
            targetDir.invalidateCache()
            targetDir.updateCache(useSu)
        } while (!targetDir.exists || !targetDir.isDirectory)

        val dirToDropPath = dirToDrop.completedPath
        val droppedFiles = mutex.withLock {
            val droppedFiles = ArrayList<XFile>()
            var droppedLast = false
            var dirToDropFound = false
            val each = files.iterator()
            while (!droppedLast && each.hasNext()) {
                val next = each.next()
                when {
                    !dirToDropFound && next.completedPath == dirToDropPath -> {
                        dirToDropFound = true
                        droppedFiles.add(next)
                    }
                    dirToDropFound && next.completedPath.startsWith(dirToDropPath) -> {
                        each.remove()
                        droppedFiles.add(next)
                    }
                    dirToDropFound -> droppedLast = true
                }
            }
            droppedFiles
        }

        if (droppedFiles.isNotEmpty()) {
            log2("dropOpenedDir $dirToDropPath")
            notifyRemoveRange(droppedFiles)
        } else {
            log2("Dir to drop was not found! $dirToDropPath")
        }
        currentOpenedDir = targetDir
        targetDir.open()
        notifyUpdate(targetDir)
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
        val items = mutex.withLock { ArrayList(files) }
        store.setAndNotify(items)
    }
}