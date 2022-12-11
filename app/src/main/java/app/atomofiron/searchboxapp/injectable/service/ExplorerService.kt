package app.atomofiron.searchboxapp.injectable.service

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.CacheConfig
import app.atomofiron.searchboxapp.model.explorer.*
import app.atomofiron.searchboxapp.model.explorer.NodeContent.Directory.Type
import app.atomofiron.searchboxapp.model.explorer.NodeRoot.NodeRootType
import app.atomofiron.searchboxapp.model.preference.ToyboxVariant
import app.atomofiron.searchboxapp.utils.*
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.close
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.completePath
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.delete
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.open
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.rename
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.sortByDate
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.sortByName
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.theSame
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.update
import app.atomofiron.searchboxapp.utils.Tool.endingDot
import app.atomofiron.searchboxapp.utils.Tool.writeTo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import java.io.File
import java.io.FileOutputStream
import java.util.*

class ExplorerService(
    context: Context,
    private val packageManager: PackageManager,
    private val assets: AssetManager,
    private val appStore: AppStore,
    private val explorerStore: ExplorerStore,
    private val preferenceStore: PreferenceStore,
) {
    companion object {
        private const val SUB_PATH_CAMERA = "DCIM/Camera/"
        private const val SUB_PATH_PICTURES = "Pictures/Screenshots/"
        private const val SUB_PATH_DOWNLOAD = "Download/"
        private const val SUB_PATH_DOWNLOAD_BLUETOOTH = "Download/Bluetooth/"
        private const val SUB_PATH_BLUETOOTH = "Bluetooth/"
    }

    private val scope = appStore.scope
    private val previewSize = context.resources.getDimensionPixelSize(R.dimen.preview_size)

    private var config = CacheConfig(useSu = false)
    private val internalStoragePath = Environment
        .getExternalStorageDirectory()
        .absolutePath
        .completePath(directory = true)

    private val states = LinkedList<NodeState>()
    private val mutex = Mutex()
    private val tab = NodeTabTree(mutex, states)

    init {
        scope.launch(Dispatchers.IO) {
            withTab {
                preferenceStore.useSu.first()
                copyToybox(context)
                initRoots()
            }
            updateRoots()
        }
        // todo try move out
        val thumbnailSize = context.resources.getDimensionPixelSize(R.dimen.thumbnail_size)
        preferenceStore.useSu.collect(scope) {
            config = CacheConfig(it, thumbnailSize)
        }
        explorerStore.current.collect(scope) {
            preferenceStore.setOpenedDirPath(it?.path)
        }
        preferenceStore.toyboxVariant.collect(scope) {
            Shell.toyboxPath = it.toyboxPath
        }
    }

    private fun NodeTabTree.initRoots() {
        val storagePath = internalStoragePath
        val roots = listOf(
            NodeRoot(NodeRootType.Photos, ExplorerDelegate.asRoot("${storagePath}$SUB_PATH_CAMERA")),
            NodeRoot(NodeRootType.Videos, ExplorerDelegate.asRoot("${storagePath}$SUB_PATH_CAMERA")),
            NodeRoot(NodeRootType.Screenshots, ExplorerDelegate.asRoot("${storagePath}$SUB_PATH_PICTURES")),
            NodeRoot(NodeRootType.Downloads, ExplorerDelegate.asRoot("${storagePath}$SUB_PATH_DOWNLOAD")),
            NodeRoot(NodeRootType.Bluetooth, ExplorerDelegate.asRoot("${storagePath}$SUB_PATH_BLUETOOTH")),
            NodeRoot(NodeRootType.InternalStorage(), ExplorerDelegate.asRoot(storagePath)),
        )
        this.roots.clear()
        this.roots.addAll(roots)
    }

    suspend fun trySelectRoot(rootItem: NodeRoot) {
        withTab {
            var index = roots.indexOfFirst { it.isSelected }
            tree.clear()
            if (index >= 0) {
                val selected = roots[index]
                roots[index] = selected.copy(isSelected = false)
                if (selected.stableId == rootItem.stableId) {
                    return@withTab
                }
            }
            index = roots.indexOfFirst { it.stableId == rootItem.stableId }
            val root = roots[index]
            var item = root.item.copy(children = root.item.children?.copy(isOpened = true))
            roots[index] = root.copy(item = item, isSelected = true)
            tree.add(NodeLevel(item.parentPath, mutableListOf(item)))
            while (true) {
                val children = item.children?.items ?: mutableListOf()
                val level = NodeLevel(item.path, children)
                tree.add(level)
                item = level.getOpened() ?: break
            }
        }
    }

    suspend fun tryToggle(it: Node) {
        withTab {
            var item = tree.findNode(it.uniqueId)
            item = when {
                item?.isCached != true -> return
                item.isOpened -> item.children?.find { it.isOpened } ?: item
                else -> item
            }
            val (levelIndex, level) = tree.findIndexed(item.parentPath)
            if (levelIndex < 0 || level == null) return

            val targetIndex = level.children.indexOfFirst { it.uniqueId == item.uniqueId }
            if (targetIndex < 0) return

            val openedIndex = level.getOpenedIndex()
            if (openedIndex >= 0 && openedIndex != targetIndex) {
                val opened = level.children[openedIndex]
                level.children[openedIndex] = opened.close()
            }
            for (i in levelIndex.inc()..tree.lastIndex) {
                tree.removeAt(tree.lastIndex)
            }
            val target = level.children[targetIndex]
            val children = item.children
            level.children[targetIndex] = when {
                target.isOpened -> target.close()
                children == null -> return
                else -> {
                    tree.add(NodeLevel(target.path, children.items))
                    target.open()
                }
            }
        }
    }

    suspend fun updateRoots() {
        withTab {
            updateInternalStorageStats()
            roots.forEach { root ->
                updateRootAsync(root)
            }
        }
    }

    private suspend fun NodeTabTree.updateRootAsync(root: NodeRoot) {
        when (root.type) {
            is NodeRootType.Photos,
            is NodeRootType.Videos,
            is NodeRootType.Screenshots -> Unit
            is NodeRootType.Bluetooth -> return updateBluetoothAsync(root)
            else -> if (root.item.isCached) return
        }
        withCachingState(root.stableId) {
            root.updateRootSync()
        }
    }

    private suspend fun NodeTabTree.updateBluetoothAsync(root: NodeRoot) {
        val storagePath = internalStoragePath
        var current = root.item
        var bluetooth = ExplorerDelegate.asRoot("${storagePath}$SUB_PATH_BLUETOOTH")
        var downloadBluetooth = ExplorerDelegate.asRoot("${storagePath}$SUB_PATH_DOWNLOAD_BLUETOOTH")
        withCachingState(root.stableId) {
            current = current.update(config).sortByDate()
            bluetooth = if (current.error == null) bluetooth.update(config).sortByDate() else bluetooth
            downloadBluetooth = if (current.error == null) downloadBluetooth.update(config).sortByDate() else downloadBluetooth
            val item = when {
                current.error == null -> current
                bluetooth.error == null -> bluetooth
                downloadBluetooth.error == null -> downloadBluetooth
                else -> current
            }
            withTab {
                states.updateState(root.stableId) {
                    nextState(root.stableId, cachingJob = null)
                }
                roots.replace {
                    when {
                        it.stableId != root.stableId -> it
                        else -> {
                            if (root.isSelected && !item.isCached) tree.clear()
                            val isSelected = root.isSelected && item.isCached
                            root.copy(isSelected = isSelected, item = item)
                        }
                    }
                }
            }
        }
    }

    private fun NodeTabTree.updateInternalStorageStats() {
        var root = roots.find { it.type is NodeRootType.InternalStorage }!!
        val statFs = StatFs(root.item.path)
        val freeBytes = statFs.freeBytes
        val totalBytes = statFs.totalBytes
        val type = (root.type as NodeRootType.InternalStorage).copy(used = totalBytes - freeBytes, free = freeBytes)
        root = root.copy(type = type)
        roots.replace(root) { it.stableId == root.stableId }
    }

    private suspend fun NodeRoot.updateRootSync() {
        val updated = item.update(config).run {
            if (withPreview) sortByDate() else sortByName()
        }
        val onlyPhotos = type == NodeRootType.Photos || type == NodeRootType.Screenshots
        val onlyVideos = type == NodeRootType.Videos
        val onlyMedia = type == NodeRootType.Camera
        if (onlyPhotos || onlyVideos || onlyMedia) {
            updated.children?.update {
                replace {
                    when {
                        onlyPhotos && it.content !is NodeContent.File.Picture -> null
                        onlyVideos && it.content !is NodeContent.File.Movie -> null
                        onlyMedia && it.content !is NodeContent.File.Movie && it.content !is NodeContent.File.Picture -> null
                        else -> it
                    }
                }
            }
        }
        val newestChild = updated.takeIf { withPreview }?.children?.firstOrNull()
        val root = when {
            newestChild == null -> copy(item = updated, thumbnail = null, thumbnailPath = "")
            thumbnailPath == newestChild.path -> this
            else -> {
                val config = config.copy(thumbnailSize = previewSize)
                val updatedChild = newestChild.copy(content = NodeContent.Unknown).update(config)
                val content = updatedChild.content as? NodeContent.File
                copy(item = updated, thumbnail = content?.thumbnail, thumbnailPath = newestChild.path)
            }
        }
        withTab {
            roots.replace {
                when {
                    it.stableId != root.stableId -> it
                    else -> {
                        if (root.isSelected && !item.isCached) tree.clear()
                        val isSelected = root.isSelected && item.isCached
                        root.copy(isSelected = isSelected, item = updated)
                    }
                }
            }
            states.updateState(root.stableId) {
                nextState(root.stableId, cachingJob = null)
            }
        }
    }

    private inline fun <T> MutableList<T>.replace(new: T?, action: (T) -> Boolean) {
        replace {
            if (action(it)) new else it
        }
    }

    private inline fun <T> MutableList<T>.replace(action: (T) -> T?) {
        val iterator = listIterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            val new = action(next)
            when {
                new === next -> Unit
                new == null -> iterator.remove()
                else -> iterator.set(new)
            }
        }
    }

    suspend fun tryCacheAsync(it: Node) {
        withTab {
            val item = tree.findNode(it.uniqueId)
            item ?: return
            val isMediaRoot = roots.find { it.item.uniqueId == item.uniqueId }?.withPreview == true
            withCachingState(item.uniqueId) {
                cacheSync(item, isMediaRoot) {
                    tree.replaceItem(it)
                }
            }
            return
        }
    }

    suspend fun tryRename(it: Node, name: String) {
        val item = withTab {
            tree.findNode(it.uniqueId)
        }
        item ?: return
        val renamed = item.rename(name, config.useSu)
        withTab {
            val (_, level) = tree.findIndexed(item.parentPath)
            val index = level?.children?.indexOfFirst { it.uniqueId == item.uniqueId }
            if (index == null || index < 0) return
            level.children[index] = renamed
        }
    }

    suspend fun tryCreate(dir: Node, name: String, directory: Boolean) {
        val item = ExplorerDelegate.create(dir, name, directory, config.useSu)
        withTab {
            val (_, level) = tree.findIndexed(dir.path)
            level?.children ?: return
            when {
                item.isDirectory -> level.children.add(0, item)
                else -> {
                    var index = level.children.indexOfFirst { it.isFile }
                    if (index < 0) index = level.children.size
                    level.children.add(index, item)
                }
            }
            explorerStore.actions.emit(NodeAction.Inserted(item.uniqueId))
        }
    }

    suspend fun tryCheckItem(item: Node, isChecked: Boolean) {
        withTab {
            val (_, state) = states.findIndexed(item.uniqueId)
            if (state?.withOperation == true) return
            if (!checked.tryUpdateCheck(item.uniqueId, isChecked)) return
        }
    }

    suspend fun tryMarkInstalling(item: Node, installing: Operation.Installing?): Boolean {
        return withTab {
            var state = states.find { it.uniqueId == item.uniqueId }
            if (state?.operation == installing) return false
            state = states.updateState(item.uniqueId) {
                nextState(item.uniqueId, installing = installing)
            }
            state?.operation == installing
        }
    }

    /** @return action succeed */
    private fun MutableList<Int>.tryUpdateCheck(uniqueId: Int, makeChecked: Boolean): Boolean {
        val iter = iterator()
        while (iter.hasNext()) {
            val item = iter.next()
            when {
                item != uniqueId -> Unit
                makeChecked -> return false
                else -> {
                    iter.remove()
                    return true
                }
            }
        }
        if (makeChecked) add(uniqueId)
        return makeChecked
    }

    suspend fun tryDelete(its: List<Node>) {
        var mediaRootAffected: NodeRoot? = null
        val items = withTab {
            mediaRootAffected = roots.find { it.isSelected && it.withPreview }
            its.mapNotNull { item ->
                tree.findNode(item.uniqueId)?.takeIf {
                    val state = states.updateState(item.uniqueId) {
                        when (this?.isDeleting) {
                            true -> null
                            else -> {
                                this?.cachingJob?.cancel()
                                checked.tryUpdateCheck(item.uniqueId, makeChecked = false)
                                nextState(item.uniqueId, cachingJob = null, deleting = Operation.Deleting)
                            }
                        }
                    }
                    state?.isDeleting == true
                }
            }
        }
        val jobs = items.map { item ->
            scope.launch {
                delay(1000)
                val result = item.delete(config.useSu)
                withTab {
                    tree.replaceItem(item.uniqueId, item.parentPath, result)
                    states.updateState(item.uniqueId) { null }
                    when (result) {
                        null -> explorerStore.actions.emit(NodeAction.Removed(item.uniqueId))
                        else -> explorerStore.actions.emit(NodeAction.Updated(item.uniqueId))
                    }
                }
            }
        }
        jobs.forEach { it.join() }
        mediaRootAffected?.let {
            withTab {
                updateRootAsync(it)
            }
        }
    }

    suspend fun tryReceive(where: Node, uri: Uri) {
        val inputStream = appStore.context.contentResolver.openInputStream(uri)
        inputStream ?: return
        val outputStream = FileOutputStream(File(""))
        val success = inputStream.writeTo(outputStream)
        inputStream.close()
        outputStream.close()
    }

    private suspend inline fun <R> withTab(block: NodeTabTree.() -> R): R {
        return tab.updateTab {
            val result = block()

            states.replace {
                if (it.withoutState) null else it
            }

            tree.dropClosedLevels()
            updateDirectoryTypes()
            val currentDir = getCurrentDir()
            val items = renderNodes()
            explorerStore.items.emit(NodeTabItems(roots.toMutableList(), items, currentDir))
            explorerStore.current.value = currentDir

            updateStates(items)
            updateChecked(items)
            val checked = items.filter { it.isChecked }
            explorerStore.checked.set(checked)

            result
        }
    }

    private fun MutableList<NodeLevel>.dropClosedLevels() {
        var parentPath = ExplorerDelegate.ROOT_PARENT_PATH
        var chained = true
        for (i in indices) {
            when {
                !chained -> removeLast()
                get(i).parentPath != parentPath -> {
                    chained = false
                    removeLast()
                }
                else -> when (val opened = get(i).getOpened()) {
                    null -> chained = false
                    else -> parentPath = opened.path
                }
            }
        }
    }

    private fun NodeTabTree.updateStates(items: List<Node>) {
        if (states.isNotEmpty()) {
            val iterator = states.listIterator()
            while (iterator.hasNext()) {
                val state = iterator.next()
                if (state.withoutState) continue
                var item = roots.find { it.stableId == state.uniqueId }?.item
                item = item ?: items.find { it.uniqueId == state.uniqueId }
                if (item == null) {
                    state.cachingJob?.cancel()
                    val next = state.nextState(state.uniqueId, cachingJob = null)
                    iterator.updateState(state, next)
                }
            }
        }
    }

    private fun NodeTabTree.updateChecked(items: List<Node>) {
        if (checked.isNotEmpty()) {
            val iterator = checked.listIterator()
            while (iterator.hasNext()) {
                val uniqueId = iterator.next()
                val item = items.find { it.uniqueId == uniqueId }
                if (item == null) iterator.remove()
            }
        }
    }

    private fun NodeTabTree.renderNodes(): List<Node> {
        var isEmpty = false
        val count = tree.sumOf { it.count }
        val items = ArrayList<Node>(count)
        for (i in tree.indices) {
            val level = tree[i]
            for (j in 0..level.getOpenedIndex()) {
                var item = updateStateFor(level.children[j])
                if (item.isOpened && i == tree.lastIndex.dec()) {
                    item = item.copy(isCurrent = true)
                    isEmpty = item.isEmpty
                }
                items.add(item)
            }
        }
        var skip = true
        for (i in tree.indices.reversed()) {
            val level = tree[i]
            when {
                isEmpty -> isEmpty = false
                skip -> skip = false
                else -> tree.getOrNull(i.dec())?.getOpened()?.let {
                    val path = it.path.endingDot()
                    val item = Node(path, it.parentPath, rootId = it.rootId, children = it.children, properties = it.properties, content = it.content)
                    items.add(item)
                }
            }
            for (j in level.getOpenedIndex().inc() until level.count) {
                items.add(updateStateFor(level.children[j]))
            }
        }
        return items
    }

    private fun NodeTabTree.updateStateFor(item: Node): Node {
        val state = states.find { it.uniqueId == item.uniqueId }
        val isChecked = checked.find { it == item.uniqueId } != null
        return when {
            state != null -> item.copy(state = state, isChecked = isChecked)
            isChecked -> item.copy(isChecked = true)
            else -> item
        }
    }

    private fun NodeTabTree.updateDirectoryTypes() {
        val defaultStoragePath = internalStoragePath
        val (_, level) = tree.findIndexed(defaultStoragePath)
        level ?: return
        for (i in level.children.indices) {
            val item = level.children[i]
            val content = item.content as? NodeContent.Directory
            content ?: continue
            if (content.type != Type.Ordinary) continue
            val type = ExplorerDelegate.getDirectoryType(item.name)
            if (type == Type.Ordinary) continue
            level.children[i] = item.copy(content = content.copy(type = type))
        }
    }

    private fun NodeTabTree.getCurrentDir(): Node? {
        val item = tree.findLast { it.getOpened() != null }?.getOpened()
        return item?.let { updateStateFor(it) }
    }

    private fun NodeTabTree.withCachingState(id: Int, async: suspend CoroutineScope.() -> Unit) {
        var state = states.find { it.uniqueId == id }
        if (state != null) return
        val job = scope.launch(block = async)
        state = states.updateState(id) {
            nextState(id, cachingJob = job)
        }
        if (state?.isCaching != true) job.cancel()
    }

    private suspend fun CoroutineScope.cacheSync(item: Node, isMediaRoot: Boolean, predicate: NodeTabTree.(Node) -> Boolean) {
        if (!isActive) return
        var updated = item.update(config).run {
            if (isMediaRoot) sortByDate() else sortByName()
        }
        updated = updated.updateContent()
        withTab {
            states.updateState(item.uniqueId) {
                nextState(item.uniqueId, cachingJob = null)
            }
            val current = tree.findNode(item.uniqueId)
            current ?: return
            if (updated.isOpened != current.isOpened) {
                updated = updated.copy(children = updated.children?.copy(isOpened = current.isOpened))
            }
            if (!predicate(updated)) return
            explorerStore.actions.emit(NodeAction.Updated(item.uniqueId))
        }
    }

    private fun NodeState?.nextState(
        uniqueId: Int,
        cachingJob: Job? = this?.cachingJob,
        deleting: Operation.Deleting? = this?.operation as? Operation.Deleting,
        copying: Operation.Copying? = this?.operation as? Operation.Copying,
        installing: Operation.Installing? = this?.operation as? Operation.Installing,
    ): NodeState? {
        val nextOperation = when (this?.operation ?: Operation.None) {
            is Operation.None -> deleting ?: copying ?: installing
            is Operation.Deleting -> deleting ?: copying
            is Operation.Copying -> copying ?: deleting
            is Operation.Installing -> installing ?: deleting
        } ?: Operation.None

        return when {
            cachingJob == null && nextOperation is Operation.None -> null
            theSame(cachingJob, nextOperation) -> return this
            else -> NodeState(uniqueId, cachingJob, nextOperation)
        }
    }

    private fun MutableList<NodeState>.updateState(
        uniqueId: Int,
        block: NodeState?.() -> NodeState?,
    ): NodeState? {
        val (index, state) = findIndexed(uniqueId)
        val new = state.block()
        when {
            state == null && new != null -> add(new)
            state != null && new == null -> removeAt(index)
            state != null && new != null -> set(index, new)
        }
        return new
    }

    private fun MutableListIterator<NodeState>.updateState(current: NodeState?, new: NodeState?) {
        when {
            current == null && new != null -> add(new)
            current != null && new == null -> remove()
            current != null && new != null -> set(new)
        }
    }

    private fun List<NodeLevel>.replaceItem(item: Node) = replaceItem(item.uniqueId, item.parentPath, item)

    private fun List<NodeLevel>.replaceItem(uniqueId: Int, parentPath: String, item: Node?): Boolean {
        val (levelIndex, level) = findIndexed(parentPath)
        val index = level?.children?.indexOfFirst { it.uniqueId == uniqueId }
        if (index == null || index < 0) return false
        val prev = level.children[index]
        val wasOpened = prev.isOpened
        if (prev.areContentsTheSame(item)) return false
        val upLevel = getOrNull(levelIndex.dec())
        when (item?.isOpened) {
            null -> level.children.removeAt(index)
            wasOpened -> level.children[index] = item
            else -> level.children[index] = item.copy(children = item.children?.copy(isOpened = wasOpened))
        }
        // далее заменяем/удаляем айтем в родительской ноде
        val parent = upLevel?.children?.find { it.path == parentPath }
        val cached = parent?.children?.findIndexed { it.uniqueId == uniqueId }
        val cachedIndex = cached?.first ?: -1
        val cachedItem = cached?.second
        parent?.children?.update {
            when {
                cachedItem == null -> Unit // добавление обрабатывается отдельно, учитывая, что папки первые
                item == null -> removeAt(cachedIndex)
                else -> set(cachedIndex, item)
            }
        }
        return true
    }

    private fun List<NodeLevel>.findNode(uniqueId: Int): Node? {
        for (i in indices.reversed()) {
            get(i).children.find {
                it.uniqueId == uniqueId
            }?.let { item ->
                return item
            }
        }
        return null
    }

    private fun List<NodeLevel>.findIndexed(parentPath: String): Pair<Int, NodeLevel?> = this@findIndexed.findIndexed { it.parentPath == parentPath }

    private fun List<NodeState>.findIndexed(uniqueId: Int): Pair<Int, NodeState?> = this@findIndexed.findIndexed { it.uniqueId == uniqueId }

    private fun Node.updateContent(): Node {
        val content = content
        return when {
            content is NodeContent.File.Apk && content.thumbnail == null -> {
                val info = packageManager.getPackageArchiveInfo(path, 0)
                info ?: return this
                info.applicationInfo.sourceDir = path
                info.applicationInfo.publicSourceDir = path
                val new = NodeContent.File.Apk(
                    thumbnail = info.applicationInfo.loadIcon(packageManager),
                    appName = info.applicationInfo.loadLabel(packageManager).toString(),
                    versionName = info.versionName,
                    versionCode = info.versionCode,
                )
                copy(content = new)
            }
            else -> this
        }
    }

    private fun copyToybox(context: Context) {
        val variants = arrayOf(
            Const.VALUE_TOYBOX_ARM_32,
            Const.VALUE_TOYBOX_ARM_64,
            Const.VALUE_TOYBOX_X86_64,
        )
        context.filesDir.mkdirs()

        for (variant in variants) {
            val path = ToyboxVariant.getToyboxPath(context, variant)
            val file = File(path)
            if (file.exists() && file.canExecute()) {
                continue
            }
            val input = assets.open("toybox/" + file.name)
            val bytes = input.readBytes()
            input.close()
            val output = FileOutputStream(file)
            output.write(bytes)
            output.close()
            file.setExecutable(true, true)
        }
    }
}