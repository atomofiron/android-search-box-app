package app.atomofiron.searchboxapp.model.explorer

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class NodeTab(
    val mutex: Mutex,
    val states: MutableList<NodeState>,
) {

    val levels = mutableListOf<NodeLevel>()
    val checked: MutableList<Int> = LinkedList()

    suspend inline fun <R> updateTree(block: NodeTab.() -> R): R {
        return mutex.withLock {
            block()
        }
    }
}