package ru.atomofiron.regextool.model.finder

import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.utils.Const

class FinderResult(
        item: XFile,
        val matches: List<Int>? = null
) : XFile {
    override val completedPath = item.completedPath
    override val isDirectory: Boolean = item.isDirectory
    override val isFile = item.isFile
    override val mHashCode: Int = item.mHashCode

    override val access = item.access
    override val owner = item.owner
    override val group = item.group
    override val size = item.size
    override val date = item.date
    override val time = item.time
    override val name = item.name
    override val suffix = item.suffix

    override var isChecked: Boolean = false

    // does not matter
    override val files: List<XFile>? = null
    override val isOpened: Boolean = false
    override val isCached: Boolean = true
    override val isCacheActual: Boolean = true
    override val exists: Boolean = true
    override val completedParentPath: String = "/"
    override val root: Int = -1
    override val isRoot: Boolean = false
    override val isDeleting: Boolean = false

    fun toMarkdown(): String? {
        val name = if (isDirectory) name + Const.SLASH else name
        return String.format("[%s](%s)\n", name, completedPath.replace(" ", "\\ "))
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            !is FinderResult -> false
            else -> other.mHashCode == mHashCode
        }
    }

    override fun hashCode(): Int = completedPath.hashCode() + root + (if (isDirectory) 1 else 0)

    override fun toString(): String = completedPath
}