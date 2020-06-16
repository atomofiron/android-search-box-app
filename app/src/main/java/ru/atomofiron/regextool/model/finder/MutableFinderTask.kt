package ru.atomofiron.regextool.model.finder

import java.util.*
import kotlin.collections.ArrayList

class MutableFinderTask private constructor(
        override val uuid: UUID,
        override val id: Long,
        override val results: MutableList<FinderResult> = ArrayList(),
        override var count: Int = 0,
        override var inProgress: Boolean = true,
        override var isDone: Boolean = false,
        override var isSecondary: Boolean = false,
        override var isRemovable: Boolean = true,
        override var error: String? = null
) : FinderTask {
    companion object {
        private var lastId = 0L
        private fun nextId(): Long = lastId++

        fun secondary(isRemovable: Boolean): MutableFinderTask {
            return MutableFinderTask(UUID.randomUUID(), nextId(), inProgress = false, isDone = true, isSecondary = true, isRemovable = isRemovable)
        }
    }

    constructor(uuid: UUID) : this(uuid, nextId())

    override fun copyTask(): FinderTask {
        return MutableFinderTask(uuid, id, ArrayList(results), count, inProgress, isDone, isSecondary, isRemovable, error)
    }

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