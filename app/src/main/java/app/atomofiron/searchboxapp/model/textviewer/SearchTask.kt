package app.atomofiron.searchboxapp.model.textviewer

import app.atomofiron.searchboxapp.model.finder.SearchParams
import app.atomofiron.searchboxapp.model.finder.SearchResult
import java.util.*

sealed class SearchTask {

    abstract val uuid: UUID
    val uniqueId: Int get() = uuid.hashCode()

    abstract val params: SearchParams
    abstract val result: SearchResult
    abstract val isLocal: Boolean

    open val inProgress: Boolean get() = false
    open val isDone: Boolean get() = false
    open val isError: Boolean get() = false
    open val isRemovable: Boolean get() = false

    val count: Int get() = result.count

    data class Progress(
        override val uuid: UUID,
        override val isLocal: Boolean,
        override val params: SearchParams,
        override val result: SearchResult,
    ) : SearchTask() {
        override val inProgress = true

        constructor(isLocal: Boolean, params: SearchParams, result: SearchResult) : this(UUID.randomUUID(), isLocal, params, result)

        override fun copyWith(result: SearchResult): SearchTask = copy(result = result)
    }

    sealed class Ended : SearchTask()

    data class Error(
        override val uuid: UUID,
        val error: String,
        override val isLocal: Boolean,
        override val params: SearchParams,
        override val result: SearchResult,
    ) : Ended() {
        override val isError = true

        override fun copyWith(result: SearchResult): SearchTask = copy(result = result)
    }

    data class Done(
        override val uuid: UUID,
        val isCompleted: Boolean,
        override val isRemovable: Boolean,
        override val isLocal: Boolean,
        override val params: SearchParams,
        override val result: SearchResult,
    ) : Ended() {
        override val isDone = true

        override fun copyWith(result: SearchResult): SearchTask = copy(result = result)
    }

    abstract fun copyWith(result: SearchResult): SearchTask

    override fun hashCode(): Int = Objects.hash(this::class, result.hashCode(), uuid)

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other !is SearchTask -> false
        other::class != this::class -> false
        other.result.count != result.count -> false
        other.result.countMax != result.countMax -> false
        else -> other.uuid == uuid
    }
}

fun SearchTask.toError(error: String, result: SearchResult = this.result): SearchTask.Error {
    return SearchTask.Error(uuid, error, isLocal, params, result)
}

fun SearchTask.toDone(
    isCompleted: Boolean,
    isRemovable: Boolean = true,
    result: SearchResult = this.result,
): SearchTask.Done {
    return SearchTask.Done(uuid, isCompleted, isRemovable, isLocal, params, result)
}
