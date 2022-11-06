package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.searchboxapp.utils.Explorer.name
import app.atomofiron.searchboxapp.utils.Explorer.parent


data class Node constructor(
    val path: String,
    val parentPath: String = path.parent(),
    val uniqueId: Int = path.toUniqueId(),
    val root: Int = uniqueId,
    val children: NodeChildren? = null,

    val properties: NodeProperties = NodeProperties(name = path.name()),
    val content: NodeContent,
    val error: NodeError? = null,
) : INodeProperties by properties {
    companion object {
        private fun String.toUniqueId(): Int = hashCode()
    }
    val isRoot: Boolean = uniqueId == root

    val isDirectory: Boolean = content is NodeContent.Directory
    val isArchive: Boolean = content is NodeContent.File.Archive
    val isFile: Boolean = content is NodeContent.File

    val isCached: Boolean get() = children != null
    val isEmpty: Boolean get() = children?.isEmpty() == true
    val isOpened: Boolean get() = children?.isOpened == true
    // todo checked
    val isChecked: Boolean = false

    fun areContentsTheSame(other: Node): Boolean = when {
        other.uniqueId != uniqueId -> false
        other.path != path -> false
        other.root != root -> false
        other.properties != properties -> false
        other.error != error -> false
        other.isCached != isCached -> false
        other.isEmpty != isEmpty -> false
        other.isOpened != isOpened -> false
        other.isDirectory != isDirectory -> false
        other.isArchive != isArchive -> false
        other.isFile != isFile -> false
        else -> true
    }

    override fun hashCode(): Int = uniqueId

    override fun equals(other: Any?): Boolean = when {
        other !is Node -> false
        !areContentsTheSame(other) -> false
        else -> other.children?.equals(children) == true
    }

    fun rename(name: String): Node {
        val path = parentPath + name
        val properties = properties.copy(name = name)
        return copy(path = path, uniqueId = path.toUniqueId(), properties = properties)
    }
}

