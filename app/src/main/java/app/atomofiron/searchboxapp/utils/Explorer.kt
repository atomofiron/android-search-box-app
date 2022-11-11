package app.atomofiron.searchboxapp.utils

import android.content.Context
import app.atomofiron.searchboxapp.model.explorer.*
import app.atomofiron.searchboxapp.model.explorer.NodeContent.Directory.Type
import kotlinx.coroutines.Job

object Explorer {
    const val ROOT_PARENT_PATH = "root_parent_path"

    private const val UNKNOWN = -1
    private const val TOTAL = "total"
    private const val SLASH = "/"
    private const val NEW_LINE = "\n"
    private const val ROOT = SLASH
    private const val ROOT_NAME = "Root"
    private const val DIR_CHAR = 'd'
    private const val LINK_CHAR = 'l'
    private const val FILE_CHAR = '-'
    private const val LS_NO_SUCH_FILE = "ls: %s: No such file or directory"
    private const val LS_PERMISSION_DENIED = "ls: %s: Permission denied"
    private const val COMMAND_PATH_PREFIX = "[a-z]+: %s: "

    private val spaces = Regex(" +")
    private val slashes = Regex("/+")
    private val lastPart = Regex("(?<=/)/*[^/]+/*$|^/+\$")
    private val endingSlashes = Regex("/*$")

    fun Context.getExternalStorageDirectory(): String? {
        val absolutePath = getExternalFilesDir(null)?.absolutePath
        absolutePath ?: return null
        val index = absolutePath.indexOf(Const.ANDROID_DIR).inc()
        return when (index) {
            0 -> null
            else -> absolutePath.substring(0, index)
        }
    }

    fun completePath(absolutePath: String, isDirectory: Boolean): String {
        return when {
            absolutePath == ROOT -> ROOT
            isDirectory -> absolutePath.replace(endingSlashes, SLASH)
            else -> absolutePath.replace(endingSlashes, "")
        }
    }

    fun String.parent(): String = replace(lastPart, "")

    fun String.name(): String = split(slashes).findLast { it.isNotEmpty() } ?: ROOT_NAME

    fun completeDirPath(absolutePath: String): String = completePath(absolutePath, isDirectory = true)

    fun create(parent: Node, name: String, isDirectory: Boolean): Node {
        val content = when {
            isDirectory -> NodeContent.Directory(Type.Ordinary)
            else -> NodeContent.File.Other
        }
        return Node(
            rootId = parent.rootId,
            path = parent.path + name,
            parentPath = parent.path,
            properties = NodeProperties(name = name),
            content = content,
        )
    }

    fun create(parent: Node, name: String, directory: Boolean, useSu: Boolean): Node {
        var targetPath = parent.path + name
        if (directory) {
            targetPath = completeDirPath(targetPath)
        }
        val output = when {
            directory -> Shell.exec(Shell[Shell.MKDIR].format(targetPath), useSu)
            else -> Shell.exec(Shell[Shell.TOUCH].format(targetPath), useSu)
        }
        val content = when {
            directory -> NodeContent.Directory()
            else -> NodeContent.File.Other
        }
        val item = Node(path = targetPath, parentPath = parent.path, rootId = parent.rootId, content = content)
        return when {
            output.success -> item.update(useSu)
            else -> item.copy(error = output.error.toNodeError(targetPath))
        }
    }

    fun asRoot(path: String): Node {
        return Node(
            path = path,
            parentPath = ROOT_PARENT_PATH,
            properties = NodeProperties(name = path.name()),
            content = NodeContent.Unknown,
        )
    }

    private fun parse(line: String, name: String? = null): NodeProperties {
        val parts = line.split(spaces, 8)
        // todo links parts[7].contains('->')
        return NodeProperties(
            access = parts[0],
            owner = parts[2],
            group = parts[3],
            size = parts[4],
            date = parts[5],
            time = parts[6],
            name = name ?: parts[7],
        )
    }

    private fun parse(parentPath: String, line: String, root: Int): Node {
        val properties = parse(line)
        val content = when (properties.access[0]) {
            DIR_CHAR -> NodeContent.Directory(Type.Ordinary)
            LINK_CHAR -> NodeContent.Link
            else -> NodeContent.File.Other
        }
        val asDir = content is NodeContent.Directory
        return Node(
            rootId = root,
            path = completePath(parentPath + properties.name, asDir),
            parentPath = parentPath,
            properties = properties,
            content = content,
        )
    }

    fun Node.hasChild(item: Node): Boolean {
        return children?.find { it.uniqueId == item.uniqueId } != null
    }

    fun getDirectoryType(name: String): Type {
        return when (name) {
            "Android" -> Type.Android
            "DCIM" -> Type.Camera
            "Download" -> Type.Download
            "Movies" -> Type.Movies
            "Music" -> Type.Music
            "Pictures" -> Type.Pictures
            else -> Type.Ordinary
        }
    }

    fun Node.update(su: Boolean): Node {
        val output = Shell.exec(Shell[Shell.LS_LAHLD].format(path), su)
        val lines = output.output.split("\n").filter { it.isNotEmpty() }
        return when {
            output.success && lines.size == 1 -> parseNode(lines.first()).run {
                if (isCached) this else cache(su)
            }
            output.success -> copy(children = null, error = NodeError.Unknown)
            else -> copy(error = output.error.toNodeError(path))
        }
    }

    private fun Node.cache(su: Boolean): Node {
        return when {
            isDirectory -> cacheDir(su)
            else -> this // caching files
        }
    }

    private fun Node.cacheDir(su: Boolean): Node {
        val output = Shell.exec(Shell[Shell.LS_LAHL].format(path), su)
        val lines = output.output.split("\n").filter { it.isNotEmpty() }
        return when {
            output.success && lines.isEmpty() -> copy(children = null, error = NodeError.Unknown)
            output.success -> parseDir(lines)
            else -> copy(error = output.error.toNodeError(path))
        }
    }

    fun Node.sortByName(): Node {
        children?.items?.run {
            sortBy { it.name.lowercase() }
            sortBy {
                when {
                    it.isDirectory -> 0
                    it.isArchive -> 1
                    else -> 2
                }
            }
        }
        return this
    }

    private fun Node.parseNode(line: String): Node {
        val properties = parse(line, name)
        val (children, content) = when {
            properties.isDirectory() -> when (content) {
                is NodeContent.Directory -> children to content
                else -> null to NodeContent.Directory()
            }
            properties.isLink() -> when (content) {
                is NodeContent.Link -> children to content
                else -> null to NodeContent.File.Other // todo file types
            }
            properties.isFile() -> when (content) {
                is NodeContent.File -> children to content
                else -> null to NodeContent.Link
            }
            else -> null to NodeContent.Unknown
        }
        return copy(children = children, properties = properties, content = content)
    }

    private fun Node.parseDir(lines: List<String>): Node {
        val items = ArrayList<Node>(lines.size)
        val files = ArrayList<Node>(lines.size)
        val start = if (lines.first().startsWith(TOTAL)) 1 else 0
        for (i in start until lines.size) {
            val line = lines[i]
            if (line.isNotEmpty()) {
                val properties = parse(line)
                val child = children?.find { it.name == properties.name }
                val item = when {
                    child == null -> parse(path, line, rootId)
                    child.properties == properties -> child
                    else -> child.copy(properties = properties)
                }
                when {
                    item.isDirectory -> items.add(item)
                    else -> files.add(item)
                }
            }
        }
        items.addAll(files)
        val directoryType = when (content) {
            is NodeContent.Directory -> content.type
            else -> Type.Ordinary
        }
        return copy(
            children = NodeChildren(items, isOpened = children?.isOpened == true),
            content = NodeContent.Directory(directoryType),
        )
    }

    private fun Node.open(value: Boolean): Node = when {
        children == null -> this
        children.isOpened == value -> this
        else -> {
            if (!value) children.clearChildren()
            copy(children = children.copy(isOpened = value))
        }
    }

    fun Node.open(): Node = open(true)

    fun Node.close(): Node = open(false)

    fun Node.isParentOf(other: Node): Boolean = other.parentPath == path

    fun Node.isTheDeepest(): Boolean = isOpened && children != null && children.find { it.isOpened } == null

    fun NodeChildren.clearChildren() {
        for (i in items.indices) {
            val child = items[i]
            if (child.isCached) {
                items[i] = child.copy(children = null)
            }
        }
    }

    fun NodeProperties.isFile(): Boolean = access.firstOrNull() == FILE_CHAR

    fun NodeProperties.isDirectory(): Boolean = access.firstOrNull() == DIR_CHAR

    fun NodeProperties.isLink(): Boolean = access.firstOrNull() == LINK_CHAR

    fun Node.delete(useSu: Boolean): Node? {
        val output = Shell.exec(Shell[Shell.RM_RF].format(path), useSu)
        return when {
            output.success -> null
            else -> copy(error = output.error.toNodeError(path))
        }
    }

    fun Node.rename(name: String, useSu: Boolean): Node {
        val targetPath = parentPath + name
        val output = Shell.exec(Shell[Shell.MV].format(path, targetPath), useSu)
        return when {
            output.success -> rename(name)
            else -> copy(error = output.error.toNodeError(path))
        }
    }

    private fun String.toNodeError(path: String): NodeError {
        val lines = split(NEW_LINE).filter { it.isNotBlank() }
        val first = lines.firstOrNull()
        return when {
            lines.size > 1 -> NodeError.Multiply
            first.isNullOrBlank() -> NodeError.Unknown
            path.isBlank() -> NodeError.Message(first)
            first == LS_NO_SUCH_FILE.format(path) -> NodeError.NoSuchFile
            first == LS_PERMISSION_DENIED.format(path) -> NodeError.PermissionDenied
            else -> NodeError.Message(first.replace(Regex(COMMAND_PATH_PREFIX.format(path)), ""))
        }
    }

    fun NodeState?.theSame(cachingJob: Job?, isChecked: Boolean, isDeleting: Boolean): Boolean {
        return when {
            this?.cachingJob !== cachingJob -> false
            (this?.isChecked ?: false) != isChecked -> false
            (this?.isDeleting ?: false) != isDeleting -> false
            else -> true
        }
    }
}
