package app.atomofiron.searchboxapp.model.finder

import app.atomofiron.searchboxapp.model.explorer.MutableXFile
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.utils.Const

class FinderResult(
        item: MutableXFile,
        val count: Int = 0
) : XFile {

    private val xFile = item

    override val completedPath = xFile.completedPath
    override val isDirectory: Boolean = xFile.isDirectory
    override val isFile = xFile.isFile
    override val mHashCode: Int = xFile.mHashCode

    override val access: String get() = xFile.access
    override val owner: String get() = xFile.owner
    override val group: String get() = xFile.group
    override val size: String get() = xFile.size
    override val date: String get() = xFile.date
    override val time: String get() = xFile.time
    override val name: String get() = xFile.name
    override val suffix: String get() = xFile.suffix
    override val isCached: Boolean get() = xFile.isCached

    override var isChecked: Boolean = false
    override val isDeleting: Boolean get() = xFile.isDeleting

    // does not matter
    override val children: List<XFile>? = null
    override val isOpened: Boolean = false
    override val isCacheActual: Boolean = true
    override val exists: Boolean = true
    override val completedParentPath: String = "/"
    override val root: Int = -1
    override val isRoot: Boolean = false
    // does not matter

    fun toMarkdown(): String? {
        val name = if (isDirectory) name + Const.SLASH else name
        return String.format("[%s](%s)\n", name, completedPath.replace(" ", "\\ "))
    }

    fun willBeDeleted() = xFile.willBeDeleted()

    fun delete(su: Boolean) = xFile.delete(su)

    fun updateCache(useSu: Boolean) = xFile.updateCache(useSu, completely = true)

    override fun equals(other: Any?): Boolean {
        return when (other) {
            !is FinderResult -> false
            else -> other.mHashCode == mHashCode
        }
    }

    override fun hashCode(): Int = xFile.hashCode()

    override fun toString(): String = xFile.toString()
}