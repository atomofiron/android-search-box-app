package app.atomofiron.searchboxapp.model.explorer

import kotlinx.coroutines.Job

data class NodeState(
    // fields hidden from view
    val uniqueId: Int,
    val cachingJob: Job? = null,
    override val operation: Operation = Operation.None,
) : INodeState {
    val withoutState: Boolean = cachingJob == null && operation is Operation.None

    val isCaching: Boolean = cachingJob != null
    val withOperation: Boolean = operation !is Operation.None
    override val isDeleting: Boolean = operation is Operation.Deleting
}

sealed class Operation {
    object None : Operation()
    object Deleting : Operation()
    class Copying(
        val isSource: Boolean,
        val asMoving: Boolean,
    ) : Operation()
}

interface INodeState {
    val isDeleting: Boolean
    val operation: Operation?
}