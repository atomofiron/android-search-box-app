package ru.atomofiron.regextool.model.finder

import ru.atomofiron.regextool.model.explorer.MutableXFile
import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.utils.Const

class FinderResult(
        item: MutableXFile,
        val count: Int = 0,
        val finderQueryParams: FinderQueryParams? = null
) : XFile {

    private val xFile = item

    override val completedPath = xFile.completedPath
    override val isDirectory: Boolean = xFile.isDirectory
    override val isFile = xFile.isFile
    override val mHashCode: Int = xFile.mHashCode

    override val access = xFile.access
    override val owner = xFile.owner
    override val group = xFile.group
    override val size = xFile.size
    override val date = xFile.date
    override val time = xFile.time
    override val name = xFile.name
    override val suffix = xFile.suffix

    override var isChecked: Boolean = false
    override val isDeleting: Boolean get() = xFile.isDeleting

    // does not matter
    override val children: List<XFile>? = null
    override val isOpened: Boolean = false
    override val isCached: Boolean = true
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

    override fun equals(other: Any?): Boolean {
        return when (other) {
            !is FinderResult -> false
            else -> other.mHashCode == mHashCode
        }
    }

    override fun hashCode(): Int = xFile.hashCode()

    override fun toString(): String = xFile.toString()
}