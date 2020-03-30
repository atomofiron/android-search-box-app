package ru.atomofiron.regextool.iss.service.explorer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.atomofiron.regextool.App
import ru.atomofiron.regextool.iss.service.explorer.model.MutableXFile
import ru.atomofiron.regextool.iss.service.explorer.model.XFile
import ru.atomofiron.regextool.iss.store.ExplorerStore
import ru.atomofiron.regextool.iss.store.SettingsStore
import ru.atomofiron.regextool.log2
import ru.atomofiron.regextool.utils.Shell
import java.io.File
import java.io.FileOutputStream

open class PrivateExplorerServiceLogic(
        protected val explorerStore: ExplorerStore,
        protected val settingsStore: SettingsStore
) {

    protected val mutex = Mutex()
    protected val files: MutableList<MutableXFile> get() = explorerStore.items
    protected val checked: MutableList<MutableXFile> get() = explorerStore.checked
    protected var currentOpenedDir: MutableXFile? = null
        set(value) {
            field = value
            explorerStore.notifyCurrent(value)
        }

    private val useSu: Boolean get() = settingsStore.useSu.value

    init {
        GlobalScope.launch(Dispatchers.IO) {
            mutex.withLock {
                copyToybox()
            }
        }
    }

    protected fun invalidateDir(dir: MutableXFile) {
        log2("invalidateDir $dir")
        dir.invalidateCache()
    }

    protected fun findItem(f: XFile): MutableXFile? = findItem(f.completedPath, f.root)

    private fun findParentDir(f: XFile): MutableXFile? = findItem(f.completedParentPath, f.root)

    private fun findItem(completedPath: String, root: Int): MutableXFile? {
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

    private fun copyToybox() {
        val pathToybox = App.pathToybox
        val toybox = File(pathToybox)
        toybox.deleteRecursively()
        toybox.parentFile!!.mkdirs()
        val input = App.context.assets.open("toybox/toybox64")
        val bytes = input.readBytes()
        input.close()
        val output = FileOutputStream(toybox)
        output.write(bytes)
        output.close()
        Shell.exec(Shell.NATIVE_CHMOD_X.format(pathToybox))
    }

    protected suspend fun reopenDir(dir: MutableXFile) {
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
            explorerStore.notifyUpdate(childDir)
            updateCurrentDir(dir)
        }
    }

    protected suspend fun openDir(dir: MutableXFile) {
        require(dir.isDirectory) { IllegalArgumentException("Is not a directory! $dir") }
        log2("openDir $dir")
        val anotherDir = findOpenedDirInParentOf(dir)
        if (anotherDir != null) {
            log2("openDir $dir anotherDir == $anotherDir")
            anotherDir.close()
            anotherDir.clearChildren()
            removeAllChildren(anotherDir)
            explorerStore.notifyUpdate(anotherDir)
        } else {
            log2("anotherDir == null $dir")
        }

        var anotherRoot = findOpenedAnotherRoot(dir.root)
        while (anotherRoot != null) {
            anotherRoot.close()
            anotherRoot.clearChildren()
            removeAllChildren(anotherRoot)
            explorerStore.notifyUpdate(anotherRoot)
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
        explorerStore.notifyUpdate(dir)
        if (dirFiles.isNotEmpty()) {
            explorerStore.notifyInsertRange(dir, dirFiles)
        }
        updateCurrentDir(dir)
    }

    protected suspend fun closeDir(d: XFile) {
        val dir = findItem(d) ?: return log2("closeDir not found $d")
        log2("closeDir $dir")
        val parent = if (dir.isRoot) null else findParentDir(dir)
        mutex.withLock {
            dir.close()
            currentOpenedDir = parent
        }

        dir.clearChildren()
        removeAllChildren(dir)
        explorerStore.notifyUpdate(dir)

        if (parent != null) {
            parent.invalidateCache()
            updateCurrentDir(parent)
        }
    }

    protected suspend fun updateFile(file: MutableXFile) {
        log2("updateTheFile $file")
        require(!file.isDirectory) { IllegalArgumentException("Is is a directory! $file") }
        when {
            file.updateCache(useSu) == null -> explorerStore.notifyUpdate(file)
            !file.exists -> dropEntity(file)
        }
    }

    protected suspend fun updateCurrentDir(dir: MutableXFile) {
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
                return explorerStore.notifyUpdate(dir)
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
            explorerStore.notifyUpdate(dir)
            notifyCurrentDirChanges(dir, dirFiles, newFiles)
        }
    }

    private fun notifyCurrentDirChanges(dir: MutableXFile, dirFiles: List<XFile>?, newFiles: List<MutableXFile>) {
        val wasNullOrEmpty = dirFiles.isNullOrEmpty()
        val nowIsEmpty = newFiles.isEmpty()
        when {
            wasNullOrEmpty && !nowIsEmpty -> explorerStore.notifyInsertRange(dir, newFiles)
            !wasNullOrEmpty && nowIsEmpty -> explorerStore.notifyRemoveRange(dirFiles!!)
            !wasNullOrEmpty && !nowIsEmpty -> {
                dirFiles!!
                val news = newFiles.iterator()
                val olds = dirFiles.iterator()
                var previous: XFile = dir

                while (olds.hasNext()) {
                    val next = olds.next()
                    if (!newFiles.contains(next)) {
                        explorerStore.notifyRemove(next)
                    }
                }
                while (news.hasNext()) {
                    val next = news.next()
                    if (!dirFiles.contains(next)) {
                        explorerStore.notifyInsert(previous, next)
                    }
                    previous = next
                }
            }
        }
    }

    protected suspend fun updateClosedDir(dir: MutableXFile) {
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
                explorerStore.notifyUpdate(dir)
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
            false -> explorerStore.notifyRemoveRange(removed)
        }
    }

    private suspend fun dropEntity(entity: MutableXFile) {
        log2("dropEntity $entity")
        entity.clear()
        mutex.withLock {
            files.remove(entity)
        }
        explorerStore.notifyRemove(entity)
    }

    protected fun checkChildren(dir: MutableXFile) {
        log2("checkChildren $dir")
        val dirFiles = dir.files!!
        dirFiles.forEach {
            it.isChecked = true
            checked.add(it)
        }
        explorerStore.notifyUpdateRange(dirFiles)
    }

    protected fun uncheckChildren(dir: MutableXFile): Boolean {
        log2("uncheckChildren $dir")
        val dirFiles = dir.files!!
        var containedChecked = false
        dirFiles.filter { it.isChecked }.forEach {
            containedChecked = true
            it.isChecked = false
            checked.remove(it)
        }
        if (containedChecked) {
            dir.isChecked = false
            explorerStore.notifyUpdate(dir)
            explorerStore.notifyUpdateRange(dirFiles)
        }

        return containedChecked
    }

    protected fun uncheckParent(item: MutableXFile) {
        log2("uncheckParent $item")
        checked.find {
            item.completedParentPath.startsWith(it.completedPath)
        }?.let { checkedParent ->
            checkedParent.isChecked = false
            checked.remove(checkedParent)
            explorerStore.notifyUpdate(checkedParent)
        }
    }
}