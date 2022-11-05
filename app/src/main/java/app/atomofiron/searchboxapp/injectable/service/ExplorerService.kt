package app.atomofiron.searchboxapp.injectable.service

import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import app.atomofiron.common.util.flow.value
import app.atomofiron.searchboxapp.injectable.store.AppStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.*
import app.atomofiron.searchboxapp.model.preference.ToyboxVariant
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.Explorer
import app.atomofiron.searchboxapp.utils.Explorer.ROOT_PARENT_PATH
import app.atomofiron.searchboxapp.utils.Explorer.cache
import app.atomofiron.searchboxapp.utils.Explorer.close
import app.atomofiron.searchboxapp.utils.Explorer.open
import app.atomofiron.searchboxapp.utils.Explorer.parent
import app.atomofiron.searchboxapp.utils.Explorer.sortByName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import java.io.File
import java.io.FileOutputStream
import java.util.LinkedList

class ExplorerService(
    context: Context,
    private val assets: AssetManager,
    appStore: AppStore,
    private val preferences: SharedPreferences,
    private val explorerStore: ExplorerStore,
    private val preferenceStore: PreferenceStore
) {

    private val scope = appStore.scope
    private val mutexLevels = Mutex()
    private val mutexOperations = Mutex()
    private val mutexJobs = Mutex()

    private var currentOpenedDir: Node? = null
        set(value) {
            field = value
            explorerStore.current.value = value
        }

    private val levels = mutableListOf<NodeLevel>()
    private val states = LinkedList<NodeState>()
    private val jobs = LinkedList<NodeJob>()

    private val useSu: Boolean get() = preferenceStore.useSu.value

    init {
        scope.launch(Dispatchers.IO) {
            mutexLevels.withLock {
                copyToybox(context)
            }
        }
    }

    private suspend inline fun withStates(block: LinkedList<NodeState>.() -> Unit) {
        mutexOperations.withLock {
            states.block()
        }
    }

    private suspend inline fun withJobs(block: MutableList<NodeJob>.() -> Unit) {
        mutexJobs.withLock {
            jobs.block()
        }
    }

    private suspend inline fun withLevels(block: MutableList<NodeLevel>.() -> ActionResult) {
        mutexLevels.withLock {
            when (levels.block()) {
                is ActionResult.Cancel -> Unit
                is ActionResult.Update -> levels.run {
                    dropClosedLevels()
                    val items = renderNodes()
                    explorerStore.items.value = items
                    updateCurrentDir()
                    items.dropJobs()
                }
            }
        }
    }

    private fun MutableList<NodeLevel>.dropClosedLevels() {
        var parentPath = ROOT_PARENT_PATH
        var chained = true
        for (i in indices) {
            val level = get(i)
            when {
                !chained -> removeLast()
                level.parentPath != parentPath -> {
                    chained = false
                    removeLast()
                }
                else -> when (val opened = level.getOpened()) {
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

    private fun List<NodeLevel>.renderNodes(): List<Node> {
        val count = sumOf { it.count }
        val items = ArrayList<Node>(count)
        for (i in levels.indices) {
            val level = levels[i]
            for (j in 0..level.getOpenedIndex()) {
                items.add(level.children[j])
            }
        }
        for (i in levels.indices.reversed()) {
            val level = levels[i]
            for (j in level.getOpenedIndex().inc() until level.count) {
                items.add(level.children[j])
            }
        }
        return items
    }

    private fun List<NodeLevel>.updateCurrentDir() {
        explorerStore.current.value = findLast { it.getOpened() != null }?.getOpened()
    }

    fun persistState() {
        preferences
            .edit()
            .putString(Const.PREF_CURRENT_DIR, currentOpenedDir?.path)
            .apply()
    }

    suspend fun trySetRoots(paths: Collection<String>) {
        val roots = paths.parMapMut {
            Explorer.asRoot(it)
                .cache(useSu)
                .sortByName()
        }
        withLevels {
            clear()
            add(NodeLevel(Explorer.ROOT_PARENT_PATH, roots))
            ActionResult.Update
        }
    }

    private fun findNode(uniqueId: Int): Node? {
        for (level in levels) {
            val node = level.children.find { it.uniqueId == uniqueId }
            if (node != null) return node
        }
        return null
    }

    private fun findNode(path: String): Node? {
        val parentPath = path.parent()
        return levels.find {
            it.parentPath == parentPath
        }?.children?.find {
            it.path == path
        }
    }

    suspend fun tryToggle(item: Node) {
        withLevels {
            val levelIndex = indexOfFirst { it.parentPath == item.parentPath }
            if (levelIndex < 0) return@withLevels ActionResult.Cancel
            val level = get(levelIndex)

            val targetIndex = level.children.indexOfFirst { it.uniqueId == item.uniqueId }
            if (targetIndex < 0) return@withLevels ActionResult.Cancel

            val openedIndex = level.getOpenedIndex()
            if (openedIndex >= 0 && openedIndex != targetIndex) {
                val opened = level.children[openedIndex]
                level.children[openedIndex] = opened.close()
            }

            for (i in levelIndex.inc()..lastIndex) {
                removeAt(lastIndex).getOpenedIndex()
            }
            val target = level.children[targetIndex]
            level.children[targetIndex] = when {
                target.isOpened -> target.close()
                item.children == null -> return@withLevels ActionResult.Cancel
                else -> {
                    add(NodeLevel(target.path, item.children.items))
                    target.open()
                }
            }
            val a = level.children[targetIndex]
            ActionResult.Update
        }
    }

    suspend fun tryCacheAsync(item: Node) {
        val jobs = LinkedList<NodeJob>()
        withStates {
            when (find { it.uniqueId == item.uniqueId }) {
                null -> {
                    add(NodeState.Caching(item.uniqueId))
                    val job = scope.launch { cacheSync(item) }
                    jobs.add(NodeJob(item.uniqueId, job))
                }
                is NodeState.Caching -> Unit
                is NodeState.Deleting -> Unit
            }
        }
        withJobs {
            addAll(jobs)
        }
    }

    private suspend fun CoroutineScope.cacheSync(item: Node) {
        if (!isActive) return
        val cached = item.cache(useSu).sortByName()
        withStates {
            when (val state = find { it.uniqueId == item.uniqueId }) {
                null -> Unit
                is NodeState.Caching -> remove(state)
                is NodeState.Deleting -> Unit
            }
        }
        replaceItem(cached)
    }

    private suspend fun replaceItem(item: Node) {
        withLevels {
            val level = find { it.parentPath == item.parentPath }
            level ?: return@withLevels ActionResult.Cancel
            val index = level.children.indexOfFirst { it.uniqueId == item.uniqueId }
            if (index < 0) return@withLevels ActionResult.Cancel
            val wasOpened = level.children[index].isOpened
            level.children[index] = when (item.isOpened) {
                wasOpened -> item
                else -> item.copy(children = item.children?.copy(isOpened = wasOpened))
            }
            ActionResult.Update
        }
    }

    suspend fun tryOpenParent() {
    }

    fun tryCheckItem(it: Node, isChecked: Boolean) {
    }

    suspend fun tryDelete(its: List<Node>) {
    }

    suspend fun tryRename(it: Node, name: String) {
    }

    suspend fun tryCreate(it: Node, name: String, directory: Boolean) {
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