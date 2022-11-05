package app.atomofiron.searchboxapp.model.explorer

import android.graphics.Bitmap
import app.atomofiron.searchboxapp.utils.Explorer.name
import app.atomofiron.searchboxapp.utils.Explorer.parent
import java.util.Objects


data class Children(
    val items: MutableList<Node>,
    val isOpened: Boolean = false,
) : List<Node> by items {

    override fun hashCode(): Int = Objects.hash(isOpened, items.map { it.path })

    override fun equals(other: Any?): Boolean {
        return when {
            other !is Children -> false
            other.isOpened != isOpened -> false
            other.items.size != items.size -> false
            else -> {
                for (i in items.indices) {
                    if (!other.items[i].areContentsTheSame(items[i])) {
                        return false
                    }
                }
                return true
            }
        }
    }
}

sealed class NodeContent {

    object Unknown : NodeContent()
    object Link : NodeContent()

    data class Directory(val type: DirectoryType = DirectoryType.Ordinary) : NodeContent()

    sealed class File : NodeContent() {
        data class Archive(val children: List<Node>? = null) : File()
        data class Apk(val icon: Bitmap?, val versionName: String, val versionCode: Int) : File()
        data class Picture(val thumbnail: Bitmap? = null) : File()
        data class Music(val duration: Int, val cover: Bitmap? = null) : File()
        object Text : File()
        object Other : File()
    }
}

enum class DirectoryType {
    Ordinary,
    Android,
    Camera,
    Download,
    Movies,
    Music,
    Pictures,
}

// -rw-r-----  1 root everybody   5348187 2019-06-13 18:19 Magisk-v19.3.zip
interface INodeProperties {
    val access: String
    val owner: String
    val group: String
    val size: String
    val date: String
    val time: String
    val name: String
}

class NodeProperties(
    override val access: String = "",
    override val owner: String = "",
    override val group: String = "",
    override val size: String = "",
    override val date: String = "",
    override val time: String = "",
    override val name: String = "",
) : INodeProperties

data class Node constructor(
    val path: String,
    val parentPath: String = path.parent(),
    val uniqueId: Int = path.hashCode(),
    val root: Int = uniqueId,
    val children: Children? = null,

    val properties: NodeProperties = NodeProperties(name = path.name()),
    val content: NodeContent,
    val error: NodeError? = null,
) : INodeProperties by properties {
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
        other.isCached != isCached -> false
        other.isEmpty != isEmpty -> false
        other.isOpened != isOpened -> false
        other.isDirectory != isDirectory -> false
        other.isArchive != isArchive -> false
        other.isFile != isFile -> false
        else -> true
    }

    override fun equals(other: Any?): Boolean = when {
        other !is Node -> false
        !areContentsTheSame(other) -> false
        else -> other.children?.equals(children) == true
    }
}

