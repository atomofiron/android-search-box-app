package app.atomofiron.searchboxapp.model.explorer

import kotlinx.coroutines.Job

data class NodeState(
    // fields hidden from view
    val uniqueId: Int,
    val cachingJob: Job? = null,
    override val isChecked: Boolean = false,
    override val isDeleting: Boolean = false,
) : INodeState {
    val isCaching: Boolean = cachingJob != null
    val isEmpty: Boolean = !isCaching && !isChecked && !isDeleting
}

interface INodeState {
    val isChecked: Boolean
    val isDeleting: Boolean
}