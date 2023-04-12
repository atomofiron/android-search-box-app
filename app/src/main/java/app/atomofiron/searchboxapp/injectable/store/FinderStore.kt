package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.textviewer.SearchTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FinderStore(
    private val scope: CoroutineScope,
) {
    private val mutex = Mutex()
    private val mutableTasks = MutableStateFlow(listOf<SearchTask>())
    val tasks: StateFlow<List<SearchTask>> = mutableTasks

    fun add(item: SearchTask) {
        mutableTasks.updateList {
            add(item)
        }
    }

    fun drop(item: SearchTask) {
        mutableTasks.updateList {
            remove(item)
        }
    }

    fun addOrUpdate(item: SearchTask) {
        mutableTasks.updateList {
            when (val index = indexOfFirst { it.uuid == item.uuid }) {
                -1 -> add(item)
                else -> set(index, item)
            }
        }
    }

    fun deleteResultFromTasks(item: Node) {
        mutableTasks.updateList {
            forEachIndexed { index, task ->
                val result = task.result as? SearchResult.FinderResult
                result ?: return@forEachIndexed
                val new = result.removeItem(item)
                if (new !== result) {
                    this[index] = task.copyWith(result)
                }
            }
        }
    }

    private fun <T> MutableStateFlow<List<T>>.updateList(action: MutableList<T>.(current: List<T>) -> Unit) {
        scope.launch {
            mutex.withLock {
                value = value.toMutableList().apply {
                    action(value)
                }
            }
        }
    }
}