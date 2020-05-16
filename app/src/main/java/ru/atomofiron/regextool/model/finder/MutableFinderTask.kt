package ru.atomofiron.regextool.model.finder

import java.util.*
import kotlin.collections.ArrayList

class MutableFinderTask(
        override val uuid: UUID,
        override val id: Long = nextId,
        override val results: ArrayList<FinderResult> = ArrayList(),
        override var count: Int = 0,
        override var inProgress: Boolean = true,
        override var isDone: Boolean = false,
        override var error: String? = null
) : FinderTask {
    companion object {
        private var nextId = 0L
            get() {
                field++
                return field
            }
    }

    override fun copyTask(): FinderTask = MutableFinderTask(uuid, id, ArrayList(results), count, inProgress, isDone, error)

    override fun areContentsTheSame(other: FinderTask): Boolean {
        return other.uuid == uuid &&
                other.count == count &&
                other.inProgress == inProgress &&
                other.results.size == results.size
    }

    fun dropError() {
        error = null
    }

    override fun equals(other: Any?): Boolean = when (other) {
        !is MutableFinderTask -> false
        else -> other.id == id
    }

    override fun hashCode(): Int {
        return uuid.hashCode() + count.hashCode() + inProgress.hashCode() + results.size.hashCode()
    }
}