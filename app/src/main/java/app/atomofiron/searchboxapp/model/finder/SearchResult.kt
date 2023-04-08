package app.atomofiron.searchboxapp.model.finder

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.utils.Const

class SearchResult(
    val item: Node,
    val count: Int = 0
) {

    val path = item.path
    val isDirectory: Boolean = item.isDirectory
    val mHashCode: Int = 0

    val name: String = item.name
    val isCached: Boolean = item.isCached

    var isChecked: Boolean = false
    val isDeleting: Boolean = item.state.isDeleting

    fun toMarkdown(): String {
        val name = if (isDirectory) name + Const.SLASH else name
        return String.format("[%s](%s)\n", name, path.replace(" ", "\\ "))
    }

    fun updateCache(useSu: Boolean) = Unit//xFile.updateCache(useSu, completely = true)

    override fun equals(other: Any?): Boolean {
        return when (other) {
            !is SearchResult -> false
            else -> other.mHashCode == mHashCode
        }
    }

    override fun hashCode(): Int = item.hashCode()

    override fun toString(): String = "SearchResult{count=$count,${item.path}}"
}