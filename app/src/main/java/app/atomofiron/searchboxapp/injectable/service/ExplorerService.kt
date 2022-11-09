package app.atomofiron.searchboxapp.injectable.service

import android.content.Context
import android.content.res.AssetManager
import app.atomofiron.searchboxapp.injectable.store.AppStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.*
import app.atomofiron.searchboxapp.model.explorer.NodeContent.Directory.Type
import app.atomofiron.searchboxapp.model.preference.ToyboxVariant
import app.atomofiron.searchboxapp.utils.*
import app.atomofiron.searchboxapp.utils.Explorer.close
import app.atomofiron.searchboxapp.utils.Explorer.open
import app.atomofiron.searchboxapp.utils.Explorer.rename
import app.atomofiron.searchboxapp.utils.Explorer.sortByName
import app.atomofiron.searchboxapp.utils.Explorer.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import java.io.File
import java.io.FileOutputStream
import java.util.LinkedList

class ExplorerService(
    context: Context,
    private val assets: AssetManager,
    appStore: AppStore,
    private val explorerStore: ExplorerStore,
    private val preferenceStore: PreferenceStore
) {

    private val scope = appStore.scope
    private val mutexLevels = Mutex()
    private val mutexOperations = Mutex()
    private val mutexJobs = Mutex()

    private val defaultStoragePath = Tool.getExternalStorageDirectory(context)

    private var levels = listOf<NodeLevel>()
    private val states = LinkedList<NodeState>()
    private val jobs = LinkedList<NodeJob>()

    private val useSu: Boolean get() = preferenceStore.useSu.value

    init {
        scope.launch(Dispatchers.IO) {
            mutexLevels.withLock {
                copyToybox(context)
            }
        }
        scope.launch(Dispatchers.Default) {
            explorerStore.current.collect {
                preferenceStore.openedDirPath.pushByOriginal(it?.path)
            }
        }
    }

    suspend fun trySetRoots(paths: Collection<String>) {
        val roots = paths.parMapMut {
            Explorer.asRoot(it)
                .update(useSu)
                .sortByName()
        }
        withLevels {
            clear()
            add(NodeLevel(Explorer.ROOT_PARENT_PATH, roots))
            Result.success(this)
        }
    }

    suspend fun tryToggle(it: Node) {
        withLevels {
            var item = findNode(it.uniqueId) ?: return
            if (item.isOpened) {
                val nextOpened = item.children?.find { it.isOpened }
                if (nextOpened != null) {
                    item = nextOpened
                }
            }
            val (levelIndex, level) = findLevel(item.parentPath)
            if (levelIndex < 0 || level == null) return

            val targetIndex = level.children.indexOfFirst { it.uniqueId == item.uniqueId }
            if (targetIndex < 0) return

            val openedIndex = level.getOpenedIndex()
            if (openedIndex >= 0 && openedIndex != targetIndex) {
                val opened = level.children[openedIndex]
                level.children[openedIndex] = opened.close()
            }
            for (i in levelIndex.inc()..lastIndex) {
                removeAt(lastIndex)
            }
            val target = level.children[targetIndex]
            val children = item.children
            level.children[targetIndex] = when {
                target.isOpened -> target.close()
                children == null -> return
                else -> {
                    add(NodeLevel(target.path, children.items))
                    target.open()
                }
            }
            Result.success(this)
        }
    }

    suspend fun tryCacheAsync(it: Node) {
        val item = findNode(it.uniqueId)
        item ?: return
        withStates {
            val state = updateState(item.uniqueId) {
                update(item.uniqueId, isCaching = true)
            }
            if (state?.isCaching != true) return
            val job = scope.launch { cacheSync(item) }
            withJobs {
                add(NodeJob(item.uniqueId, job))
            }
        }
    }

    suspend fun tryOpenParent() {
        withLevels {
            if (levels.size <= 1) return
            removeLast()
            val last = levels.last()
            val index = last.getOpenedIndex()
            last.children[index] = last.children[index].close()
            Result.success(this)
        }
    }

    suspend fun tryRename(it: Node, name: String) {
        val item = findNode(it.uniqueId)
        item ?: return
        val renamed = item.rename(name, useSu)
        withLevels {
            val (_, level) = findLevel(item.parentPath)
            val index = level?.children?.indexOfFirst { it.uniqueId == item.uniqueId }
            if (index == null || index < 0) return
            level.children[index] = renamed
            Result.success(this)
        }
    }

    suspend fun tryCreate(dir: Node, name: String, directory: Boolean) {
        val item = Explorer.create(dir, name, directory, useSu)
        withLevels {
            val (_, level) = findLevel(item.parentPath)
            val index = level?.children?.indexOfFirst { it.uniqueId == dir.uniqueId }
            if (index == null || index < 0) return
            val parent = level.children[index]
            parent.children ?: return
            parent.children.items.add(0, item)
            Result.success(this)
        }
    }

    suspend fun tryCheckItem(item: Node, isChecked: Boolean) {
        withStates {
            updateState(item.uniqueId) {
                update(item.uniqueId, isChecked = isChecked)
            }
        }
    }

    suspend fun tryDelete(items: List<Node>) {
    }

    private suspend inline fun withStates(block: LinkedList<NodeState>.() -> Unit) {
        mutexOperations.withLock {
            states.block()
            val iter = states.iterator()
            while (iter.hasNext()) {
                if (iter.next().isEmpty) iter.remove()
            }
        }
    }

    private suspend inline fun withJobs(block: MutableList<NodeJob>.() -> Unit) {
        mutexJobs.withLock {
            jobs.block()
        }
    }

    private suspend inline fun withLevels(block: MutableList<NodeLevel>.() -> Result<List<NodeLevel>>) {
        mutexLevels.withLock {
            levels.toMutableList().block().onSuccess { levels ->
                this.levels = levels
                levels.toMutableList().run {
                    dropClosedLevels()
                    updateDirectoryTypes()
                    val items = renderNodes()
                    explorerStore.items.set(items)
                    updateCurrentDir()
                    items.dropJobs()
                }
            }
        }
    }

    private fun MutableList<NodeLevel>.dropClosedLevels() {
        var parentPath = Explorer.ROOT_PARENT_PATH
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

    private suspend fun List<Node>.dropJobs() {
        withJobs {
            val iter = iterator()
            while (iter.hasNext()) {
                val nodeJob = iter.next()
                val index = this@dropJobs.indexOfFirst { it.uniqueId == nodeJob.uniqueId }
                if (index < 0) {
                    nodeJob.job.cancel()
                    iter.remove()
                }
            }
        }
    }

    private suspend fun List<NodeLevel>.renderNodes(): List<Node> {
        val count = sumOf { it.count }
        val items = ArrayList<Node>(count)
        withStates {
            for (i in levels.indices) {
                val level = levels[i]
                for (j in 0..level.getOpenedIndex()) {
                    items.add(updateStateFor(level.children[j]))
                }
            }
            for (i in levels.indices.reversed()) {
                val level = levels[i]
                for (j in level.getOpenedIndex().inc() until level.count) {
                    items.add(updateStateFor(level.children[j]))
                }
            }
        }
        return items
    }

    private fun List<NodeState>.updateStateFor(item: Node): Node {
        val state = find { it.uniqueId == item.uniqueId }
        state ?: return item
        return item.copy(state = state)
    }

    private fun List<NodeLevel>.updateDirectoryTypes() {
        val defaultStoragePath = defaultStoragePath ?: return
        val (_, level) = findLevel(defaultStoragePath)
        level ?: return
        for (i in level.children.indices) {
            val item = level.children[i]
            val content = item.content as? NodeContent.Directory
            content ?: continue
            if (content.type != Type.Ordinary) continue
            val type = Explorer.getDirectoryType(item.name)
            if (type == Type.Ordinary) continue
            level.children[i] = item.copy(content = content.copy(type = type))
        }
    }

    private fun List<NodeLevel>.updateCurrentDir() {
        explorerStore.current.value = findLast { it.getOpened() != null }?.getOpened()
    }

    private suspend fun CoroutineScope.cacheSync(item: Node) {
        if (!isActive) return
        val cached = item.update(useSu).sortByName()
        withStates {
            updateState(item.uniqueId) {
                update(item.uniqueId, isCaching = false)
            }
        }
        replaceItem(cached)
    }

    private inline fun MutableList<NodeState>.updateState(
        uniqueId: Int,
        block: NodeState?.() -> NodeState?,
    ): NodeState? {
        val (index, state) = findState(uniqueId)
        val new = state.block()
        when {
            state == null && new != null -> add(new)
            state != null && new == null -> removeAt(index)
            state != null && new != null -> set(index, new)
        }
        return new
    }

    private fun NodeState?.update(
        uniqueId: Int,
        isDeleting: Boolean = this?.isDeleting ?: false,
        isCaching: Boolean = (this?.isCaching ?: false) && !isDeleting,
        isChecked: Boolean = (this?.isChecked ?: false) && !isDeleting,
    ): NodeState? {
        return when {
            !isCaching && !isChecked && !isDeleting -> null
            else -> NodeState(uniqueId, isCaching, isChecked, isDeleting)
        }
    }

    private suspend fun replaceItem(item: Node) {
        withLevels {
            val (_, level) = findLevel(item.parentPath)
            val index = level?.children?.indexOfFirst { it.uniqueId == item.uniqueId }
            if (index == null || index < 0) return
            val wasOpened = level.children[index].isOpened
            level.children[index] = when (item.isOpened) {
                wasOpened -> item
                else -> item.copy(children = item.children?.copy(isOpened = wasOpened))
            }
            Result.success(this)
        }
    }

    private suspend inline fun <T, reified R> Collection<T>.parMapMut(
        crossinline action: (T) -> R,
    ): MutableList<R> {
        val arr = arrayOfNulls<R>(size)
        val jobs = mapIndexed { i, it ->
            scope.launch {
                arr[i] = action(it)
            }
        }
        jobs.forEach { it.join() }
        return MutableList(size) { i -> arr[i] as R }
    }

    private fun findNode(uniqueId: Int): Node? {
        levels.run {
            for (i in indices.reversed()) {
                get(i).children.find {
                    it.uniqueId == uniqueId
                }?.let { item ->
                    return item
                }
            }
        }
        return null
    }

    private fun List<NodeLevel>.findLevel(parentPath: String): Pair<Int, NodeLevel?> = findIndexed { it.parentPath == parentPath }

    private fun List<NodeJob>.findJob(uniqueId: Int): Pair<Int, NodeJob?> = findIndexed { it.uniqueId == uniqueId }

    private fun List<NodeState>.findState(uniqueId: Int): Pair<Int, NodeState?> = findIndexed { it.uniqueId == uniqueId }

    private fun copyToybox(context: Context) {
        val variants = arrayOf(
            Const.VALUE_TOYBOX_ARM_32,
            Const.VALUE_TOYBOX_ARM_64,
            Const.VALUE_TOYBOX_X86_64
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