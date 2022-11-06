package app.atomofiron.searchboxapp.model.explorer

import java.util.*

data class NodeChildren(
    val items: MutableList<Node>,
    val isOpened: Boolean = false,
) : List<Node> by items {

    override fun hashCode(): Int = Objects.hash(isOpened, items.map { it.path })

    override fun equals(other: Any?): Boolean {
        return when {
            other !is NodeChildren -> false
            other.isOpened != isOpened -> false
            other.items.size != items.size -> false
            else -> {
                for (i in items.indices) {
                    if (!other.items[i].areContentsTheSame(items[i])) {
                        return false
                    }
                }
                true
            }
        }
    }
}