package ru.atomofiron.regextool.iss.service

import android.preference.PreferenceManager
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.atomofiron.regextool.*
import ru.atomofiron.regextool.common.util.KObservable
import ru.atomofiron.regextool.iss.service.model.Change
import ru.atomofiron.regextool.iss.service.model.Change.*
import ru.atomofiron.regextool.iss.service.model.MutableXFile
import ru.atomofiron.regextool.iss.service.model.XFile
import ru.atomofiron.regextool.utils.Const.ROOT
import ru.atomofiron.regextool.utils.Shell
import java.io.File
import java.io.FileOutputStream
import kotlin.collections.ArrayList

class ExplorerService {
    private val sp = PreferenceManager.getDefaultSharedPreferences(App.context)

    private val mutex = Mutex()
    private val files: MutableList<MutableXFile> = ArrayList()
    private val root = MutableXFile(sp.getString(Util.PREF_STORAGE_PATH, ROOT)!!)
    private var currentOpenedDir: MutableXFile? = null

    val store = KObservable<List<XFile>>(files)
    val updates = KObservable<Change>(Nothing, single = true)

    // todo make storage
    private fun useSu() = sp.getBoolean(Util.PREF_USE_SU, false)

    init {
        GlobalScope.launch(Dispatchers.IO) {
            copyToybox()

            mutex.withLock {
                files.add(root)
            }
            updateClosedDir(root)

            notifyFiles()
        }
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
        sp.edit().putString(Util.PREF_CURRENT_DIR, currentOpenedDir?.completedPath).apply()
    }

    suspend fun updateFile(f: XFile) {
        val file = findFile(f) ?: return
        log2("updateFile $file")
        when {
            file.isOpened -> Unit // todo look for new files in opened dir
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

    private fun notifyUpdate(file: XFile) {
        log2("notifyUpdate $file")
        updates.notifyObservers(Update(file))
    }

    private fun notifyRemove(file: XFile) {
        log2("notifyRemove $file")
        updates.notifyObservers(Remove(file))
    }

    private fun notifyInsert(previous: XFile, file: XFile) {
        log2("notifyInsert $file after $previous")
        updates.notifyObservers(Insert(previous, file))
    }

    private fun notifyRemoveRange(files: List<XFile>) {
        log2("notifyRemoveRange ${files.size}")
        updates.notifyObservers(RemoveRange(files))
    }

    private fun notifyInsertRange(previous: XFile, files: List<XFile>) {
        log2("notifyInsert ${files.size} after $previous")
        updates.notifyObservers(InsertRange(previous, files))
    }

    private suspend fun notifyFiles() {
        log2("notifyFiles")
        val items = mutex.withLock { ArrayList(files) }
        store.notifyObservers(items)
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
        }

        mutex.withLock {
            dir.open()
            currentOpenedDir = dir
            invalidateDir(dir)

            val dirFiles = dir.files!!
            if (dirFiles.isNotEmpty()) {
                val index = files.indexOf(dir)
                files.addAll(index.inc(), dirFiles)
                notifyInsertRange(dir, dirFiles)
            }
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
        val su = useSu()
        val error = dir.updateCache(su)
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
                while (news.hasNext() || olds.hasNext()) {
                    var new: MutableXFile? = news.next()
                    var old: XFile? = olds.next()
                    while (old != new) {
                        if (old != null && !newFiles.contains(old)) {
                            notifyRemove(old)
                            old = if (olds.hasNext()) olds.next() else null
                        }
                        if (new != null && !dirFiles.contains(new)) {
                            notifyInsert(previous, new)
                            previous = new
                            new = if (news.hasNext()) news.next() else null
                        }
                    }

                    if (new != null) {
                        previous = new
                        new.invalidateCache()
                        notifyUpdate(new)
                    }
                }
            }
        }
    }

    private suspend fun updateClosedDir(dir: MutableXFile) {
        val su = useSu()
        require(dir.isDirectory) { IllegalArgumentException("Is not a directory! $dir") }
        if (dir.isOpened) {
            log2("updateClosedDir return $dir")
            return
        }
        log2("updateClosedDir $dir")
        val error = dir.updateCache(su)

        if (error != null) {
            log2("updateClosedDir error != null ${dir.completedPath}\n$error")
            if (!dir.exists) {
                dropEntity(dir)
            }
        } else {
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

    private suspend fun removeAllChildren(
            dir: XFile,
            cancelIf: ((List<MutableXFile>) -> Boolean)? = null
    ) {
        removeAllChildren(dir.completedPath, cancelIf)
    }

    private suspend fun removeAllChildren(
            path: String,
            cancelIf: ((List<MutableXFile>) -> Boolean)? = null
    ) {
        val removed = mutex.withLock {
            if (cancelIf?.invoke(files) == true) {
                return
            }
            val removed = ArrayList<XFile>()
            val each = files.iterator()
            loop@ while (each.hasNext()) {
                val next = each.next()
                when {
                    next.completedPath == ROOT -> Unit
                    next.completedParentPath.startsWith(path) -> {
                        each.remove()
                        removed.add(next)
                    }
                    removed.isNotEmpty() -> break@loop
                }
            }
            removed
        }
        if (removed.isNotEmpty()) {
            notifyRemoveRange(removed)
        }
    }

    private suspend fun dropOpenedDir(d: MutableXFile) {
        var dirToRemove = d.file
        var dir = d.file.parentFile
        while (!dir.exists() || !dir.isDirectory) {
            dirToRemove = dir
            dir = dir.parentFile
        }

        val dirToRemovePath = MutableXFile.completePathAsDir(dirToRemove.absolutePath)
        val targetFound = mutex.withLock {
            var targetFound = false
            val each = files.iterator()
            while (each.hasNext()) {
                val next = each.next()
                if (!targetFound) {
                    targetFound = next.completedPath == dirToRemovePath
                }
                if (next.completedPath.startsWith(dirToRemovePath)) {
                    each.remove()
                }
            }
            targetFound
        }

        if (targetFound) {
            log2("dropOpenedDir $dirToRemovePath")
            currentOpenedDir = findFile(MutableXFile.completePathAsDir(dir.absolutePath))
            notifyFiles()
        } else {
            log2("Target opened dir was already dropped! $dirToRemovePath")
        }
    }

    private suspend fun dropEntity(entity: MutableXFile) {
        log("dropEntity $entity")
        entity.clear()
        mutex.withLock {
            files.remove(entity)
        }
        notifyRemove(entity)
    }

    private fun copyToybox() {
        val toyboxPath = "${App.context.filesDir}/toybox"
        MutableXFile.toyboxPath = toyboxPath
        val toybox = File(toyboxPath)
        toybox.deleteRecursively()
        toybox.parentFile.mkdirs()
        val input = App.context.assets.open("toybox/toybox64")
        val bytes = input.readBytes()
        input.close()
        val output = FileOutputStream(toybox)
        output.write(bytes)
        output.close()
        Shell.exec(Shell.NATIVE_CHMOD_X.format(toyboxPath))
    }
}