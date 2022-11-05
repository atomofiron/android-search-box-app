package app.atomofiron.searchboxapp.model.finder

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.utils.Const

class FinderResult(
    val item: Node,
    val count: Int = 0
) {

    val completedPath = ""
    val isDirectory: Boolean = false
    val isFile = ""
    val mHashCode: Int = 0

    val access: String get() = ""
    val owner: String get() = ""
    val group: String get() = ""
    val size: String get() = ""
    val date: String get() = ""
    val time: String get() = ""
    val name: String get() = ""
    val suffix: String get() = ""
    val isCached: Boolean get() = false

    var isChecked: Boolean =false
    val isDeleting: Boolean get() = false

    // does not matter
    val children: List<Node>? =null
    val isOpened: Boolean =false
    val isCacheActual: Boolean =false
    val exists: Boolean =false
    val completedParentPath: String = ""
    val root: Int = 0
    val isRoot: Boolean =false
    // does not matter

    fun toMarkdown(): String? {
        val name = if (isDirectory) name + Const.SLASH else name
        return String.format("[%s](%s)\n", name, completedPath.replace(" ", "\\ "))
    }

    fun willBeDeleted() = Unit//item.setDeleting()

    fun delete(su: Boolean) = Unit//xFile.delete(su)

    fun updateCache(useSu: Boolean) = Unit//xFile.updateCache(useSu, completely = true)

    override fun equals(other: Any?): Boolean {
        return when (other) {
            !is FinderResult -> false
            else -> other.mHashCode == mHashCode
        }
    }

    override fun hashCode(): Int = item.hashCode()

    override fun toString(): String = item.toString()
}