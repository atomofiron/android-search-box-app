package app.atomofiron.searchboxapp.model.explorer

import java.util.Objects

sealed class NodeAction(val uniqueId: Int) {
    class Inserted(uniqueId: Int) : NodeAction(uniqueId)
    class Updated(uniqueId: Int) : NodeAction(uniqueId)
    class Removed(uniqueId: Int) : NodeAction(uniqueId)

    override fun equals(other: Any?): Boolean = when {
        other !is NodeAction -> false
        other.uniqueId != uniqueId -> false
        else -> true
    }

    override fun hashCode(): Int = Objects.hash(this::class, uniqueId)
}