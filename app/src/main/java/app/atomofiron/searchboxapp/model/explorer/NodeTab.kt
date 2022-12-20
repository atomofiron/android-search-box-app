package app.atomofiron.searchboxapp.model.explorer

import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.*

class NodeTabTree(
    val key: NodeTabKey,
    val states: MutableList<NodeState>,
) {

    val roots = mutableListOf<NodeRoot>()
    val tree = mutableListOf<NodeLevel>()
    val checked: MutableList<Int> = LinkedList()
    val flow = MutableSharedFlow<NodeTabItems>()
}