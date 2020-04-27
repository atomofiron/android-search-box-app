package ru.atomofiron.regextool.injectable.service.explorer

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.atomofiron.regextool.App
import ru.atomofiron.regextool.injectable.service.explorer.model.MutableXFile
import ru.atomofiron.regextool.injectable.service.explorer.model.XFile
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.log2
import ru.atomofiron.regextool.utils.Shell
import java.io.File
import java.io.FileOutputStream

abstract class PrivateExplorerServiceLogic constructor(
        private val assets: AssetManager,
        protected val explorerStore: ExplorerStore,
        protected val preferenceStore: PreferenceStore
) {
    companion object {
        private const val UNKNOWN = -1
    }

    private val mutex = Mutex()
    protected val files: MutableList<MutableXFile> get() = explorerStore.items
    protected val checked: MutableList<MutableXFile> get() = explorerStore.checked
    protected var currentOpenedDir: MutableXFile? = null
        set(value) {
            field = value
            explorerStore.notifyCurrent(value)
        }

    private val useSu: Boolean get() = preferenceStore.useSu.value

    init {
        GlobalScope.launch(Dispatchers.IO) {
            mutex.withLock {
                copyToybox()
            }
        }
    }

    suspend fun setRoots(roots: List<MutableXFile>) {
        mutex.withLock {
            removeExtraRoots(roots.map { it.root })
            mergeRoots(roots)
        }

        explorerStore.notifyItems()
    }

    // withLock
    private fun removeExtraRoots(roots: List<Int>) {
        val it = files.iterator()
        while (it.hasNext()) {
            val next = it.next()
            if (!roots.contains(next.root)) {
                if (next.isRoot) {
                    log2("removeExtraRoots $next")
                }
                it.remove()
            }
        }
    }

    // withLock
    private fun mergeRoots(roots: List<MutableXFile>) {
        if (files.isEmpty()) {
            log2("mergeRoots just add ${roots.size}")
            files.addAll(roots)
        }
        var i = -1
        val itRoot = roots.iterator()
        var nextRootItem = itRoot.next()
        loop@ while (++i < files.size) {
            val root = files[i].root
            when {
                root != nextRootItem.root -> {
                    log2("mergeRoots add $nextRootItem")
                    files.add(i, nextRootItem)
                    if (itRoot.hasNext()) {
                        nextRootItem = itRoot.next()
                    }
                }
                !itRoot.hasNext() -> {
                    log2("mergeRoots roots merged ${roots.size}")
                    break@loop
                }
                i.inc() != files.size -> nextRootItem = itRoot.next()
                else -> while (itRoot.hasNext()) {
                    log2("mergeRoots finally add $nextRootItem")
                    files.add(itRoot.next())
                }
            }
        }
    }

    protected fun invalidateDir(dir: MutableXFile) {
        log2("invalidateDir $dir")
        dir.invalidateCache()
    }

    protected fun findItem(it: XFile): MutableXFile? = findItem(it.completedPath, it.root)

    private fun findParentDir(it: XFile): MutableXFile? = findItem(it.completedParentPath, it.root)

    private fun findItem(completedPath: String, root: Int): MutableXFile? {
        val files = files
        var i = 0
        // ConcurrentModificationException
        while (i < files.size) {
            // files[i++] may return null
            val file: MutableXFile? = files[i++]
            file ?: i--
            if (file?.root == root && file.completedPath == completedPath) {
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
        val input = assets.open("toybox/toybox64")
        val bytes = input.readBytes()
        input.close()
        val output = FileOutputStream(toybox)
        output.write(bytes)
        output.close()
        val response = Shell.exec(Shell.NATIVE_CHMOD_X.format(pathToybox))
        if (response.error.isNotEmpty()) {
            log2("copyToybox error != null\n${response.error}")
        }
    }

    protected suspend fun open(item: MutableXFile) {
        if (!item.isDirectory) {
            log2("open return !isDirectory $item")
            return
        }
        if (!item.isCached) {
            log2("open return !isCached $item")
            return updateClosedDir(item)
        }
        log2("open $item")
        when {
            item == currentOpenedDir -> closeDir(item)
            item.isOpened -> reopenDir(item)
            else -> openDir(item)
        }
    }

    protected suspend fun updateItem(item: MutableXFile) = when {
        item.isOpened && item == currentOpenedDir -> updateCurrentDir(item)
        item.isOpened -> log2("updateItem return isOpened $item")
        item.isDirectory -> updateClosedDir(item)
        else -> updateFile(item)
    }

    fun checkItem(item: MutableXFile, isChecked: Boolean) {
        log2("checkItem $isChecked $item")
        item.isChecked = isChecked

        val dirFiles = item.files ?: arrayListOf()
        val isNotEmptyOpenedDir = item.isDirectory && item.isOpened && dirFiles.isNotEmpty()
        when {
            item.isRoot && item.isChecked -> {
                if (item.isOpened && !uncheckAllChildren(item)) {
                    checkChildren(item)
                }
                item.isChecked = false
                explorerStore.notifyUpdate(item)
            }
            isNotEmptyOpenedDir && !item.isChecked -> {
                checkChildren(item)
                checked.remove(item)
            }
            isNotEmptyOpenedDir && item.isChecked -> when {
                uncheckAllChildren(item) -> {
                    item.isChecked = false
                    explorerStore.notifyUpdate(item)
                }
                else -> {
                    uncheckParent(item)
                    checked.add(item)
                }
            }
            item.isChecked -> {
                uncheckParent(item)
                checked.add(item)
            }
            !item.isChecked -> checked.remove(item)
        }

        explorerStore.notifyChecked()
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
            explorerStore.notifyUpdate(childDir)
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
            explorerStore.notifyUpdate(anotherDir)
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

    protected suspend fun closeDir(it: XFile) {
        val dir = findItem(it)
        if (dir == null) {
            log2("closeDir return not found $it")
            return
        }
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

    private suspend fun updateFile(file: MutableXFile) {
        log2("updateTheFile $file")
        require(!file.isDirectory) { IllegalArgumentException("Is is a directory! $file") }
        when {
            file.isDeleting -> Unit
            file.updateCache(useSu) == null -> explorerStore.notifyUpdate(file)
            file.isRoot -> Unit
            !file.exists -> dropEntity(file)
        }
    }

    private suspend fun updateCurrentDir(dir: MutableXFile) {
        if (dir != currentOpenedDir) {
            log2("updateCurrentDir return dir != currentOpenedDir $dir")
            return
        }
        if (dir.isDeleting) {
            log2("updateCurrentDir return isDeleting $dir")
            return
        }
        val dirFiles = dir.files
        val error = dir.updateCache(useSu)
        if (error != null) {
            log2("updateCurrentDir return error != null $dir\n$error")
            if (!dir.exists) {
                closeDir(dir)
            }
            return
        }
        log2("updateCurrentDir $dir")
        val newFiles = dir.files!!

        mutex.withLock {
            if (!dir.isCached || !files.contains(dir)) {
                log2("updateCurrentDir return $dir")
                return
            }
            if (!dir.isOpened) {
                log2("updateCurrentDir return !isOpened $dir")
                return explorerStore.notifyUpdate(dir)
            }
            if (dir != currentOpenedDir) {
                log2("updateCurrentDir return !isCurrentOpenedDir $dir")
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

                while (olds.hasNext()) {
                    val next = olds.next()
                    if (!newFiles.contains(next)) {
                        explorerStore.notifyRemove(next)
                    }
                }
                while (news.hasNext()) {
                    val next = news.next()
                    if (!dirFiles.contains(next)) {
                        val index = newFiles.indexOf(next)
                        val previous = if (index == 0) dir else newFiles[index.dec()]
                        explorerStore.notifyInsert(previous, next)
                    }
                }
            }
        }
    }

    private suspend fun updateClosedDir(dir: MutableXFile) {
        if (!dir.isDirectory) {
            log2("updateClosedDir return !isDirectory $dir")
        }
        if (dir.isOpened) {
            log2("updateClosedDir return isOpened $dir")
            return
        }
        if (dir.isDeleting) {
            log2("updateClosedDir return isDeleting $dir")
            return
        }
        log2("updateClosedDir $dir")
        val cacheWasNotActual = !dir.isCacheActual
        val error = dir.updateCache(useSu)

        if (error != null) {
            log2("updateClosedDir error != null $dir\n$error")
        }
        when {
            cacheWasNotActual && dir.isRoot -> explorerStore.notifyUpdate(dir)
            dir.isRoot -> Unit
            !dir.exists -> dropEntity(dir)
            error != null -> Unit
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
                    !next.isRoot && next.root == root && next.completedParentPath.startsWith(path) -> {
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

    private fun checkChildren(dir: MutableXFile) {
        log2("checkChildren $dir")
        val dirFiles = dir.files!!
        dirFiles.forEach {
            it.isChecked = true
            checked.add(it)
        }
        explorerStore.notifyUpdateRange(dirFiles)
    }

    private fun uncheckAllChildren(dir: MutableXFile): Boolean {
        log2("uncheckChildren $dir")
        var containedChecked = false
        checked.filter { it.completedParentPath.startsWith(dir.completedPath) }.forEach {
            containedChecked = true
            it.isChecked = false
            explorerStore.notifyUpdate(it)
            checked.remove(it)
        }

        return containedChecked
    }

    private fun uncheckParent(item: MutableXFile) {
        log2("uncheckParent $item")
        checked.find {
            item.completedParentPath.startsWith(it.completedPath)
        }?.let { checkedParent ->
            checkedParent.isChecked = false
            checked.remove(checkedParent)
            explorerStore.notifyUpdate(checkedParent)
        }
    }

    protected suspend fun deleteItems(items: List<MutableXFile>) {
        log2("deleteItems ${items.size}")
        items.forEach { item ->
            if (checked.contains(item)) {
                item.isChecked = false
                checked.remove(item)
                explorerStore.notifyUpdate(item)
            }
        }
        explorerStore.notifyChecked()
        items.forEach { item ->
            deleteItem(item)
        }
    }

    private suspend fun deleteItem(item: MutableXFile) {
        if (item.isDeleting) {
            log2("deleteItem return $item")
            return
        }
        log2("deleteItem $item")
        val error = item.delete()
        if (error != null) {
            log2("deleteItem error != null $item\n$error")
        }
        when {
            item.isOpened -> updateCurrentDir(currentOpenedDir!!)
            item.isDirectory -> updateClosedDir(item)
            else -> updateItem(item)
        }
    }

    suspend fun rename(item: MutableXFile, name: String) {
        log2("rename $name $item")
        val pair = item.rename(name, useSu)
        val error = pair.first
        val newItem = pair.second
        if (error != null) {
            log2("rename error != null $item\n$error")
            explorerStore.alerts.setAndNotify(error)
        }
        if (newItem != null) {
            val parent = findParentDir(item)
            if (parent == null) {
                log2("rename return no parent $item")
                return
            }
            mutex.withLock {
                val index = files.indexOf(item)
                if (index == UNKNOWN) {
                    log2("rename return -1 $item")
                    return@withLock
                }
                val warning = parent.replace(item, newItem)
                if (warning != null) {
                    log2("rename warning != null $item\n$warning")
                }
                files.add(index.inc(), newItem)
                explorerStore.notifyInsert(item, newItem)
                files.removeAt(index)
                explorerStore.notifyRemove(item)
            }
            if (explorerStore.checked.remove(item)) {
                explorerStore.notifyChecked()
            }
        }
        if (error != null) {
            when {
                item.isOpened -> updateCurrentDir(currentOpenedDir!!)
                item.isDirectory -> updateClosedDir(item)
                else -> updateItem(item)
            }
        }
    }

    suspend fun create(dir: MutableXFile, name: String, directory: Boolean) {
        log2("create $directory $name $dir")
        val item = MutableXFile.create(dir.completedPath, name, directory, dir.root)
        val error = item.create()
        when {
            error != null -> {
                log2("create error != null $dir\n$error")
                explorerStore.alerts.setAndNotify(error)
                when {
                    dir.isOpened -> updateCurrentDir(currentOpenedDir!!)
                    dir.isDirectory -> updateClosedDir(dir)
                    else -> updateItem(dir)
                }
            }
            else -> {
                mutex.withLock {
                    val index = files.indexOf(dir)
                    if (index == UNKNOWN) {
                        log2("create return -1 $dir\n$item -> $dir")
                        return@withLock
                    }
                    val err = dir.add(item)
                    if (err != null) {
                        log2("create error != null $dir\n$err $item -> $dir")
                        return
                    }
                    files.add(index.inc(), item)
                    explorerStore.notifyInsert(dir, item)
                }
            }
        }
    }
}