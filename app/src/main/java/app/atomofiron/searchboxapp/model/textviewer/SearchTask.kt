package app.atomofiron.searchboxapp.model.textviewer

import app.atomofiron.searchboxapp.model.finder.FinderQueryParams
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.finder.FinderTask
import java.util.*

sealed class SearchTask(val queryId: Long) : FinderTask {
    companion object {
        private fun getInstantId(): Long = System.currentTimeMillis()
    }

    override val id: Long = getInstantId()
    override val isLocal = true

    class Progress(
        override val params: FinderQueryParams,
    ) : SearchTask(queryId = getInstantId()) {
        override val isRemovable = false
        override val inProgress = true
        override val isDone = false
        override val count: Int = 0

        override fun areContentsTheSame(other: FinderTask): Boolean = other is Progress
    }

    sealed class Ended(queryId: Long) : SearchTask(queryId) {
        override val inProgress = false
    }

    class Error(
        queryId: Long,
        override val params: FinderQueryParams,
        override val error: String,
    ) : Ended(queryId) {
        override val isRemovable = true
        override val isDone = false
        override val count: Int = 0

        override fun areContentsTheSame(other: FinderTask): Boolean = other is Error
    }

    class Done(
        queryId: Long,
        override val isRemovable: Boolean,
        override val params: FinderQueryParams,
        override val count: Int = 0,
        /** line index -> matches byteOffset+length */
        val matchesMap: Map<Int, List<TextLineMatch>>,
        val indexes: List<Int>,
    ) : Ended(queryId) {
        override val isDone = true

        override fun areContentsTheSame(other: FinderTask): Boolean = other is Done
    }

    override val uuid: UUID = UUID.randomUUID()
    override val results: List<SearchResult> = listOf()
    override val error: String? = null

    override fun copyTask(): FinderTask = this

    override fun hashCode(): Int = Objects.hash(this::class, id)

    override fun equals(other: Any?): Boolean = when {
        other !is SearchTask -> false
        else -> other.id == id
    }
}