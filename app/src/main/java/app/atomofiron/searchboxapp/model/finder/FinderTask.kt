package app.atomofiron.searchboxapp.model.finder

import java.util.*

interface FinderTask {
    val id: Long
    val uuid: UUID
    val params: FinderQueryParams
    val results: List<SearchResult>
    val count: Int
    val inProgress: Boolean
    val isLocal: Boolean
    val isRemovable: Boolean
    val isDone: Boolean
    val error: String?

    fun copyTask(): FinderTask
    fun areContentsTheSame(other: FinderTask): Boolean
}