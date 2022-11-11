package app.atomofiron.searchboxapp.model.explorer

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class NodeTab {
    val mutex = Mutex()

    val levels = mutableListOf<NodeLevel>()
    val states = LinkedList<NodeState>()

    suspend inline fun <R> updateTree(block: NodeTab.() -> R): R {
        return mutex.withLock {
            block()
        }
    }
}