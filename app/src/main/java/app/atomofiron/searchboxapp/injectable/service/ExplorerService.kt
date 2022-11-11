package app.atomofiron.searchboxapp.injectable.service

import android.content.Context
import android.content.res.AssetManager
import app.atomofiron.searchboxapp.injectable.store.AppStore
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
import app.atomofiron.searchboxapp.utils.Explorer.theSame
import app.atomofiron.searchboxapp.utils.Explorer.update
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class ExplorerService(
    context: Context,
    private val assets: AssetManager,
    appStore: AppStore,
    private val explorerStore: ExplorerStore,
    private val preferenceStore: PreferenceStore,
) {

    private val scope = appStore.scope

    private val defaultStoragePath = Tool.getExternalStorageDirectory(context)
    private val useSu: Boolean get() = preferenceStore.useSu.value

    private val tab = NodeTab()

    init {
        scope.launch(Dispatchers.IO) {
            withTab {
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
        withTab {
            levels.clear()
            levels.add(NodeLevel(Explorer.ROOT_PARENT_PATH, roots))
        }
    }

    suspend fun tryToggle(it: Node) {
        withTab {
            var item = levels.findNode(it.uniqueId) ?: return
            if (item.isOpened) {
                val nextOpened = item.children?.find { it.isOpened }
                if (nextOpened != null) {
                    item = nextOpened
                }
            }
            val (levelIndex, level) = levels.findLevel(item.parentPath)
            if (levelIndex < 0 || level == null) return

            val targetIndex = level.children.indexOfFirst { it.uniqueId == item.uniqueId }
            if (targetIndex < 0) return

            val openedIndex = level.getOpenedIndex()
            if (openedIndex >= 0 && openedIndex != targetIndex) {
                val opened = level.children[openedIndex]
                level.children[openedIndex] = opened.close()
            }
            for (i in levelIndex.inc()..levels.lastIndex) {
                levels.removeAt(levels.lastIndex)
            }
            val target = level.children[targetIndex]
            val children = item.children
            level.children[targetIndex] = when {
                target.isOpened -> target.close()
                children == null -> return
                else -> {
                    levels.add(NodeLevel(target.path, children.items))
                    target.open()
                }
            }
        }
    }

    suspend fun tryCacheAsync(it: Node) {
        withTab {
            val item = levels.findNode(it.uniqueId)
            item ?: return
            val job = scope.launch { cacheSync(item) }
            val state = states.updateState(item.uniqueId) {
                nextState(item.uniqueId, cachingJob = job)
            }
            if (state?.isCaching != true) job.cancel()
        }
    }

    suspend fun tryOpenParent() {
        withTab {
            if (levels.size <= 1) return
            levels.removeLast()
            val last = levels.last()
            val index = last.getOpenedIndex()
            last.children[index] = last.children[index].close()
        }
    }

    suspend fun tryRename(it: Node, name: String) {
        val item = withTab {
            levels.findNode(it.uniqueId)
        }
        item ?: return
        val renamed = item.rename(name, useSu)
        withTab {
            val (_, level) = levels.findLevel(item.parentPath)
            val index = level?.children?.indexOfFirst { it.uniqueId == item.uniqueId }
            if (index == null || index < 0) return
            level.children[index] = renamed
        }
    }

    suspend fun tryCreate(dir: Node, name: String, directory: Boolean) {
        val item = Explorer.create(dir, name, directory, useSu)
        withTab {
            val (_, level) = levels.findLevel(item.parentPath)
            val index = level?.children?.indexOfFirst { it.uniqueId == dir.uniqueId }
            if (index == null || index < 0) return
            val parent = level.children[index]
            parent.children ?: return
            parent.children.items.add(0, item)
        }
    }

    suspend fun tryCheckItem(item: Node, isChecked: Boolean) {
        withTab {
            states.updateState(item.uniqueId) {
                nextState(item.uniqueId, isChecked = isChecked)
            }
        }
    }

    suspend fun tryDelete(items: List<Node>) {
    }

    private suspend inline fun <R> withTab(block: NodeTab.() -> R): R {
        return tab.updateTree {
            val result = block()

            val iter = states.iterator()
            while (iter.hasNext()) {
                if (iter.next().isEmpty) iter.remove()
            }

            levels.dropClosedLevels()
            updateDirectoryTypes()
            val items = renderNodes()
            explorerStore.items.set(items)
            levels.updateCurrentDir()
            updateStates(items)

            result
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

    private fun NodeTab.updateStates(items: List<Node>) {
        if (states.isEmpty()) return
        val iterator = states.listIterator()
        while (iterator.hasNext()) {
            val state = iterator.next()
            if (!state.isCaching && !state.isChecked) continue
            val index = items.indexOfFirst { it.uniqueId == state.uniqueId }
            if (index < 0) {
                state.cachingJob?.cancel()
                val next = state.nextState(state.uniqueId, cachingJob = null, isChecked = false)
                iterator.updateState(state, next)
            }
        }
    }

    private fun NodeTab.renderNodes(): List<Node> {
        val count = levels.sumOf { it.count }
        val items = ArrayList<Node>(count)
        for (i in levels.indices) {
            val level = levels[i]
            for (j in 0..level.getOpenedIndex()) {
                items.add(states.updateStateFor(level.children[j]))
            }
        }
        for (i in levels.indices.reversed()) {
            val level = levels[i]
            for (j in level.getOpenedIndex().inc() until level.count) {
                items.add(states.updateStateFor(level.children[j]))
            }
        }
        return items
    }

    private fun List<NodeState>.updateStateFor(item: Node): Node {
        val state = find { it.uniqueId == item.uniqueId }
        state ?: return item
        return item.copy(state = state)
    }

    private fun NodeTab.updateDirectoryTypes() {
        val defaultStoragePath = defaultStoragePath ?: return
        val (_, level) = levels.findLevel(defaultStoragePath)
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
        withTab {
            states.updateState(item.uniqueId) {
                nextState(item.uniqueId, cachingJob = null)
            }
        }
        replaceItem(cached)
    }

    private val undefinedJob = Job()
    private fun NodeState?.nextState(
        uniqueId: Int,
        isDeleting: Boolean? = null,
        cachingJob: Job? = undefinedJob,
        isChecked: Boolean? = null,
    ): NodeState? {
        val makeDeleting = isDeleting ?: this?.isDeleting ?: false
        val withCachingJob = if (cachingJob === undefinedJob) this?.cachingJob else cachingJob
        val makeChecked = isChecked ?: this?.isChecked ?: false
        val new = when {
            withCachingJob == null && !makeChecked && !makeDeleting -> null
            theSame(withCachingJob, makeChecked, makeDeleting) -> return this
            else -> NodeState(uniqueId, withCachingJob, makeChecked, makeDeleting)
        }
        return new
    }

    private fun MutableList<NodeState>.updateState(
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

    private fun MutableListIterator<NodeState>.updateState(current: NodeState?, new: NodeState?) {
        when {
            current == null && new != null -> add(new)
            current != null && new == null -> remove()
            current != null && new != null -> set(new)
        }
    }

    private suspend fun replaceItem(item: Node) {
        withTab {
            val (_, level) = levels.findLevel(item.parentPath)
            val index = level?.children?.indexOfFirst { it.uniqueId == item.uniqueId }
            if (index == null || index < 0) return
            val wasOpened = level.children[index].isOpened
            level.children[index] = when (item.isOpened) {
                wasOpened -> item
                else -> item.copy(children = item.children?.copy(isOpened = wasOpened))
            }
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

    private fun List<NodeLevel>.findLevel(parentPath: String): Pair<Int, NodeLevel?> = findIndexed { it.parentPath == parentPath }

    private fun List<NodeState>.findState(uniqueId: Int): Pair<Int, NodeState?> = findIndexed { it.uniqueId == uniqueId }

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