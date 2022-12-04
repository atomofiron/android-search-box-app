package app.atomofiron.searchboxapp.model.explorer

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class NodeTabTree(
    val mutex: Mutex,
    val states: MutableList<NodeState>,
) {

    val roots = mutableListOf<NodeRoot>()
    val tree = mutableListOf<NodeLevel>()
    val checked: MutableList<Int> = LinkedList()

    suspend inline fun <R> updateTree(block: NodeTabTree.() -> R): R {
        return mutex.withLock {
            block()
        }
    }
}