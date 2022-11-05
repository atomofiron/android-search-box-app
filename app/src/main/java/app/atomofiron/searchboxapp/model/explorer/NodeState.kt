package app.atomofiron.searchboxapp.model.explorer

sealed class NodeState(
    val uniqueId: Int,
    val isCaching: Boolean = false,
    val isDeleting: Boolean = false,
) {
    class Caching(uniqueId: Int) : NodeState(uniqueId, isCaching = true)
    class Deleting(uniqueId: Int) : NodeState(uniqueId, isDeleting = true)
}