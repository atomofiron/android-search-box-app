package ru.atomofiron.regextool.model.finder

import java.util.*
import kotlin.collections.ArrayList

class MutableFinderTask private constructor(
        override val uuid: UUID,
        override val id: Long,
        override val results: ArrayList<FinderResult>,
        override var count: Int,
        override var inProgress: Boolean,
        override var isDone: Boolean
) : FinderTask {
    companion object {
        private var nextId = 0L
            get() {
                field++
                return field
            }
    }

    constructor(uuid: UUID) : this(uuid, nextId, results = ArrayList(), count = 0, inProgress = true, isDone = false)

    override fun copyTask(): FinderTask = MutableFinderTask(uuid, id, ArrayList(results), count, inProgress, isDone)

    override fun areContentsTheSame(other: FinderTask): Boolean {
        return other.uuid == uuid &&
                other.count == count &&
                other.inProgress == inProgress &&
                other.results.size == results.size
    }

    override fun equals(other: Any?): Boolean = when (other) {
        !is MutableFinderTask -> false
        else -> other.id == id
    }

    override fun hashCode(): Int = id.hashCode()
}