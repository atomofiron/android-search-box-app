package ru.atomofiron.regextool.model.finder

import java.util.*
import kotlin.collections.ArrayList

class MutableFinderTask(override val uuid: UUID) : FinderTask {
    companion object {
        private var nextId = 0L
            get() {
                field++
                return field
            }
    }
    override val id = nextId
    override val results = ArrayList<FinderResult>()
    override var count = 0
    override var inProgress = true

    override fun equals(other: Any?): Boolean = when (other) {
        !is MutableFinderTask -> false
        else -> other.id == id
    }

    override fun hashCode(): Int = id.hashCode()
}