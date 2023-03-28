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
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.updateWith
import app.atomofiron.searchboxapp.utils.Tool.endingDot
import app.atomofiron.searchboxapp.utils.Tool.writeTo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
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

    private val garden = NodeGarden()

    init {
        scope.launch(Dispatchers.IO) {
            garden.withGarden {
                preferenceStore.useSu.first()
                copyToybox(context)
            }
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

    suspend fun getOrCreateFlow(key: NodeTabKey): MutableSharedFlow<NodeTabItems> {
        return garden.withGarden {
            getOrCreateFlowSync(key)
        }
    }

    fun getOrCreateFlowSync(key: NodeTabKey): MutableSharedFlow<NodeTabItems> {
        return garden.run {
            trees[key]?.run { return flow }
            val tree = NodeTabTree(key, states)
            tree.initRoots()
            trees[key] = tree
            scope.launch {
                withGarden(key) { tab ->
                    tab.updateRootsAsync()
                }
            }
            tree.flow
        }
    }

    suspend fun dropTab(key: NodeTabKey) {
        garden.withGarden {
            trees.remove(key)
        }
    }

    private fun NodeTabTree.initRoots() {
        val storagePath = internalStoragePath
        val roots = listOf(
            NodeRootType.Photos.let { NodeRoot(it, ExplorerDelegate.asRoot("${storagePath}$SUB_PATH_CAMERA", it)) },
            NodeRootType.Videos.let { NodeRoot(it, ExplorerDelegate.asRoot("${storagePath}$SUB_PATH_CAMERA", it)) },
            NodeRootType.Screenshots.let { NodeRoot(it, ExplorerDelegate.asRoot("${storagePath}$SUB_PATH_PICTURES", it)) },
            NodeRootType.Bluetooth.let { NodeRoot(it, ExplorerDelegate.asRoot("${storagePath}$SUB_PATH_BLUETOOTH", it)) },
            NodeRootType.Downloads.let { NodeRoot(it, ExplorerDelegate.asRoot("${storagePath}$SUB_PATH_DOWNLOAD", it)) },
            NodeRootType.InternalStorage().let { NodeRoot(it, ExplorerDelegate.asRoot(storagePath, it)) },
        )
        this.roots.clear()
        this.roots.addAll(roots)
    }

    suspend fun trySelectRoot(key: NodeTabKey, rootItem: NodeRoot) {
        renderTab(key) {
            var index = roots.indexOfFirst { it.isSelected }
            tree.clear()
            if (index >= 0) {
                val selected = roots[index]
                roots[index] = selected.copy(isSelected = false)
                if (selected.stableId == rootItem.stableId) {
                    return@renderTab
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

    suspend fun tryToggle(key: NodeTabKey, it: Node) {
        renderTab(key) {
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

    suspend fun updateRootsAsync(key: NodeTabKey) {
        withGarden(key) { tab ->
            updateInternalStorageStats(tab)
            tab.render()
            tab.updateRootsAsync()
        }
    }

    private fun NodeTabTree.updateRootsAsync() {
        roots.forEach { root ->
            when (root.type) {
                is NodeRootType.Photos,
                is NodeRootType.Videos,
                is NodeRootType.Screenshots -> updateRootAsync(key, root)
                is NodeRootType.Bluetooth -> updateBluetoothAsync(key, root)
                else -> if (!root.item.isCached) updateRootAsync(key, root)
            }
        }
    }

    private fun updateRootAsync(key: NodeTabKey, root: NodeRoot) {
        scope.launch {
            withGarden(key) { tab ->
                withCachingState(root.stableId) {
                    launch {
                        val updated = root.item.update(config).run {
                            if (root.withPreview) sortByDate() else sortByName()
                        }
                        updateRootSync(updated, key, root)
                        tab.render()
                        trees.values.forEach { otherTab ->
                            if (otherTab.key != key) otherTab.render()
                        }
                    }
                }
            }
        }
    }

    private fun updateBluetoothAsync(key: NodeTabKey, root: NodeRoot) {
        scope.launch {
            withGarden(key) { tab ->
                updateBluetoothSync(key, root)
                tab.render()
            }
        }
    }

    private fun NodeGarden.updateBluetoothSync(key: NodeTabKey, root: NodeRoot) {
        val storagePath = internalStoragePath
        var current = root.item
        val rootType = NodeRootType.Bluetooth
        var bluetooth = ExplorerDelegate.asRoot("$storagePath$SUB_PATH_BLUETOOTH", rootType)
        var downloadBluetooth = ExplorerDelegate.asRoot("$storagePath$SUB_PATH_DOWNLOAD_BLUETOOTH", rootType)
        current = current.update(config).sortByDate()
        bluetooth = if (current.error == null) bluetooth.update(config).sortByDate() else bluetooth
        downloadBluetooth = if (current.error == null) downloadBluetooth.update(config).sortByDate() else downloadBluetooth
        val updated = when {
            current.error == null -> current
            bluetooth.error == null -> bluetooth
            downloadBluetooth.error == null -> downloadBluetooth
            else -> current
        }

        states.updateState(root.stableId) {
            nextState(root.stableId, cachingJob = null)
        }
        trees.values.forEach { tab ->
            tab.roots.replace {
                when {
                    it.stableId != root.stableId -> it
                    else -> {
                        if (it.isSelected && !updated.isCached) tab.tree.clear()
                        val isSelected = it.isSelected && updated.isCached
                        val item = if (tab.key == key) updated else it.item.updateWith(updated)
                        it.copy(isSelected = isSelected, item = item)
                    }
                }
            }
        }
    }

    private fun NodeGarden.updateInternalStorageStats(targetTab: NodeTabTree) {
        var root = targetTab.roots.find { it.type is NodeRootType.InternalStorage }!!
        val statFs = StatFs(root.item.path)
        val freeBytes = statFs.freeBytes
        val totalBytes = statFs.totalBytes
        val type = (root.type as NodeRootType.InternalStorage).copy(used = totalBytes - freeBytes, free = freeBytes)
        trees.values.forEach { tab ->
            root = tab.roots.find { it.type is NodeRootType.InternalStorage }!!
            root = root.copy(type = type)
            tab.roots.replace(root) { it.stableId == root.stableId }
        }
    }

    private fun NodeGarden.updateRootSync(updated: Node, key: NodeTabKey, targetRoot: NodeRoot) {
        val onlyPhotos = targetRoot.type == NodeRootType.Photos || targetRoot.type == NodeRootType.Screenshots
        val onlyVideos = targetRoot.type == NodeRootType.Videos
        val onlyMedia = targetRoot.type == NodeRootType.Camera
        if (onlyPhotos || onlyVideos || onlyMedia) {
            updated.children?.update {
                replace {
                    when {
                        onlyPhotos && !it.content.isPicture() -> null
                        onlyVideos && !it.content.isMovie() -> null
                        onlyMedia && !it.content.isMedia() -> null
                        else -> it
                    }
                }
            }
        }
        val newestChild = updated.takeIf { targetRoot.withPreview }?.children?.firstOrNull()
        val root = when {
            newestChild == null -> targetRoot.copy(item = updated, thumbnail = null, thumbnailPath = "")
            targetRoot.thumbnailPath == newestChild.path -> targetRoot
            else -> {
                val config = config.copy(thumbnailSize = previewSize)
                val updatedChild = newestChild.copy(content = NodeContent.Unknown).update(config)
                val content = updatedChild.content as? NodeContent.File
                targetRoot.copy(item = updated, thumbnail = content?.thumbnail, thumbnailPath = newestChild.path)
            }
        }
        states.updateState(root.stableId) {
            nextState(root.stableId, cachingJob = null)
        }
        trees.values.forEach { tab ->
            tab.roots.replace {
                when {
                    it.stableId != targetRoot.stableId -> it
                    else -> {
                        if (it.isSelected && !root.item.isCached) tab.tree.clear()
                        val isSelected = it.isSelected && root.item.isCached
                        when (tab.key) {
                            key -> root
                            else -> it.copy(
                                thumbnail = root.thumbnail,
                                thumbnailPath = root.thumbnailPath,
                                isSelected = isSelected,
                                item = it.item.updateWith(updated),
                            )
                        }
                    }
                }
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

    suspend fun tryCacheAsync(key: NodeTabKey, it: Node) {
        withGarden(key) { tab ->
            val item = tab.tree.findNode(it.uniqueId)
            item ?: return
            val isMediaRoot = tab.roots.find { it.item.uniqueId == item.uniqueId }?.withPreview == true
            withCachingState(item.uniqueId) {
                cacheSync(key, item, isMediaRoot) {
                    // todo replace everywhere
                    tree.replaceItem(it)
                }
            }
        }
    }

    suspend fun tryRename(key: NodeTabKey, it: Node, name: String) {
        val item = withTab(key) {
            tree.findNode(it.uniqueId)
        }
        item ?: return
        val renamed = item.rename(name, config.useSu)
        renderTab(key) {
            val (_, level) = tree.findIndexed(item.parentPath)
            val index = level?.children?.indexOfFirst { it.uniqueId == item.uniqueId }
            if (index == null || index < 0) return
            level.children[index] = renamed
        }
    }

    suspend fun tryCreate(key: NodeTabKey, dir: Node, name: String, directory: Boolean) {
        val item = ExplorerDelegate.create(dir, name, directory, config.useSu)
        renderTab(key) {
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

    suspend fun tryCheckItem(key: NodeTabKey, item: Node, isChecked: Boolean) {
        renderTab(key) {
            val (_, state) = states.findIndexed(item.uniqueId)
            if (state?.withOperation == true) return
            if (!checked.tryUpdateCheck(item.uniqueId, isChecked)) return
        }
    }

    suspend fun tryMarkInstalling(tab: NodeTabKey, item: Node, installing: Operation.Installing?): Boolean? {
        return withTab(tab) {
            var state = states.find { it.uniqueId == item.uniqueId }
            if (state?.operation == installing) return false
            state = states.updateState(item.uniqueId) {
                nextState(item.uniqueId, installing = installing)
            }
            render()
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

    suspend fun tryDelete(key: NodeTabKey, its: List<Node>) {
        var mediaRootAffected: NodeRoot? = null
        val items = mutableListOf<Node>()
        renderTab(key) {
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
            }.let {
                items.addAll(it)
            }
        }
        val jobs = items.map { item ->
            scope.launch {
                delay(1000)
                val result = item.delete(config.useSu)
                withGarden(key) { tab ->
                    tab.tree.replaceItem(item.uniqueId, item.parentPath, result)
                    states.updateState(item.uniqueId) { null }
                    when (result) {
                        null -> explorerStore.actions.emit(NodeAction.Removed(item.uniqueId))
                        else -> explorerStore.actions.emit(NodeAction.Updated(item.uniqueId))
                    }
                    tab.render()
                }
            }
        }
        jobs.forEach { it.join() }
        mediaRootAffected?.let { mediaRoot ->
            withGarden {
                updateRootAsync(key, mediaRoot)
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

    private suspend inline fun <R> withGarden(block: NodeGarden.() -> R): R = garden.withGarden(block)

    private suspend inline fun withGarden(key: NodeTabKey, block: NodeGarden.(NodeTabTree) -> Unit) {
        garden.withGarden {
            get(key)?.let { block(it) }
        }
    }

    private suspend inline fun <R> withTab(key: NodeTabKey, block: NodeTabTree.() -> R): R? {
        return garden.withGarden {
            get(key)?.block()
        }
    }

    private suspend inline fun renderTab(key: NodeTabKey) {
        garden.withGarden {
            val tab = get(key) ?: return
            tab.render()
        }
    }

    private suspend inline fun renderTab(key: NodeTabKey, block: NodeTabTree.() -> Unit) {
        garden.withGarden {
            val tab = get(key) ?: return
            tab.block()
            tab.render()
        }
    }

    private suspend inline fun NodeTabTree.render() {
        states.replace {
            // todo NullPointerException
            if (it.withoutState) null else it
        }
        tree.dropClosedLevels()
        updateDirectoryTypes()
        val currentDir = getCurrentDir()
        val items = renderNodes()
        val tabItems = NodeTabItems(roots.toMutableList(), items, currentDir)
        flow.emit(tabItems)
        explorerStore.current.value = currentDir

        updateStates(items)
        updateChecked(items)
        val checked = items.filter { it.isChecked }
        explorerStore.searchTargets.set(checked)
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

    /** @return already existing caching job */
    private fun NodeGarden.withCachingState(id: Int, caching: suspend CoroutineScope.() -> Unit): Job? {
        var state = states.find { it.uniqueId == id }
        if (state != null) return state.cachingJob
        val job = scope.launch(start = CoroutineStart.LAZY, block = caching)
        state = states.updateState(id) {
            nextState(id, cachingJob = job)
        }
        require(state?.cachingJob === job)
        job.start()
        return null
    }

    private suspend fun cacheSync(
        key: NodeTabKey,
        item: Node,
        isMediaRoot: Boolean,
        predicate: NodeTabTree.(Node) -> Boolean,
    ): Node {
        var updated = item.update(config).run {
            if (isMediaRoot) sortByDate() else sortByName()
        }
        updated = updated.updateContent()
        renderTab(key) {
            states.updateState(item.uniqueId) {
                nextState(item.uniqueId, cachingJob = null)
            }
            val current = tree.findNode(item.uniqueId)
            current ?: return updated
            if (updated.isOpened != current.isOpened) {
                updated = updated.copy(children = updated.children?.copy(isOpened = current.isOpened))
            }
            if (!predicate(updated)) return updated
            explorerStore.actions.emit(NodeAction.Updated(item.uniqueId))
        }
        return updated
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
        val nextJob = when (cachingJob) {
            null -> null
            else -> this?.cachingJob ?: cachingJob
        }
        return when {
            nextJob == null && nextOperation is Operation.None -> null
            theSame(nextJob, nextOperation) -> return this
            else -> NodeState(uniqueId, nextJob, nextOperation)
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
            // todo IndexOutOfBoundsException: Index: 1, Size: 1
            // todo NullPointerException: Attempt to read from field 'java.lang.Object java.util.LinkedList$Node.item' on a null object reference in method 'java.lang.Object java.util.LinkedList.unlink(java.util.LinkedList$Node)'
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
        val parent = upLevel?.children?.find { it.path == parentPath }
        if (level.children === parent?.children?.items) {
            return true
        }
        // далее заменяем/удаляем айтем в родительской ноде
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

    // todo WTF 'NodeState.getUniqueId()' on a null object reference
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