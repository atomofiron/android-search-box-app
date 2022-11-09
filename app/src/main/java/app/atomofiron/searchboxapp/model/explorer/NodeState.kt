package app.atomofiron.searchboxapp.model.explorer

data class NodeState(
    // fields hidden from view
    val uniqueId: Int,
    val isCaching: Boolean = false,
    override val isChecked: Boolean = false,
    override val isDeleting: Boolean = false,
) : INodeState {
    val isEmpty: Boolean = !isCaching && !isChecked && !isDeleting
}

interface INodeState {
    val isChecked: Boolean
    val isDeleting: Boolean
}