package app.atomofiron.searchboxapp.utils

import android.content.Context
import app.atomofiron.searchboxapp.model.explorer.*
import app.atomofiron.searchboxapp.model.explorer.Node.Companion.toUniqueId
import app.atomofiron.searchboxapp.poop

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
    private const val LS_S = "ls: %s: "
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
            isDirectory -> NodeContent.Directory(DirectoryType.Ordinary)
            else -> NodeContent.File.Other
        }
        return Node(
            root = parent.root,
            path = parent.path + name,
            parentPath = parent.path,
            properties = NodeProperties(name = name),
            content = content,
        )
    }

    fun create(parent: Node, name: String, directory: Boolean, useSu: Boolean): Node {
        var targetPath = parent.path + name
        poop("create $directory ${parent.path} + $name")
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
        val item = Node(path = targetPath, parentPath = parent.path, root = parent.root, content = content)
        return when {
            output.success -> item.cache(useSu)
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

    private fun parse(line: String): NodeProperties {
        val parts = line.split(spaces, 8)
        val access = parts[0]
        val owner = parts[2]
        val group = parts[3]
        val size = parts[4]
        val date = parts[5]
        val time = parts[6]
        val name = parts[7]
        // todo links parts[7].contains('->')
        return NodeProperties(access, owner, group, size, date, time, name)
    }

    private fun parse(parentPath: String, line: String, root: Int): Node {
        val properties = parse(line)
        val content = when (properties.access[0]) {
            DIR_CHAR -> NodeContent.Directory(DirectoryType.Ordinary)
            LINK_CHAR -> NodeContent.Link
            else -> NodeContent.File.Other
        }
        val asDir = content is NodeContent.Directory
        return Node(
            root = root,
            path = completePath(parentPath + properties.name, asDir),
            parentPath = parentPath,
            properties = properties,
            content = content,
        )
    }

    fun Node.hasChild(item: Node): Boolean {
        return children?.find { it.uniqueId == item.uniqueId } != null
    }

    /*fun Node.update(other: Node): Node = when {
        other === this -> this
        else -> {
            val content = when {
                other.content === content -> content
                else -> content.update(other.content)
            }
            other.copy(state = state, content = content)
        }
    }*/

    fun Context.getDirectoryType(path: String): DirectoryType {
        val storage = Tool.getExternalStorageDirectory(this) ?: Const.ROOT
        return when (path) {
            "${storage}Android/" -> DirectoryType.Android
            "${storage}DCIM/" -> DirectoryType.Camera
            "${storage}Download/" -> DirectoryType.Download
            "${storage}Movies/" -> DirectoryType.Movies
            "${storage}Music/" -> DirectoryType.Music
            "${storage}Pictures/" -> DirectoryType.Pictures
            else -> DirectoryType.Ordinary
        }
    }

    fun Node.cache(su: Boolean): Node {
        val output = Shell.exec(Shell[Shell.LS_LAHL].format(path), su)
        val lines = output.output.split("\n").filter { it.isNotEmpty() }
        val isList = lines.size > 1 || output.output.startsWith(TOTAL)
        return when {
            output.success && isList -> parseDir(lines)
            output.success && lines.isEmpty() -> copy(children = null, error = NodeError.Unknown)
            output.success -> parseFile(lines.first())
            output.error == LS_NO_SUCH_FILE.format(path) -> copy(children = null, error = NodeError.NoSuchFile)
            output.error == LS_PERMISSION_DENIED.format(path) -> copy(children = null, error = NodeError.PermissionDenied)
            else -> copy(children = null, error = NodeError.Message(output.error.replace(LS_S.format(path), "")))
        }
    }

    fun Node.sortByName(): Node {
        children?.items?.run {
            sortBy { it.name.lowercase() }
            reverse()
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

    private fun Node.parseFile(line: String): Node {
        val properties = parse(line)
        val content = when {
            properties.isDirectory() -> when (content) {
                is NodeContent.Directory -> content
                else -> NodeContent.Directory()
            }
            properties.isLink() -> when (content) {
                is NodeContent.Link -> content
                else -> NodeContent.File.Other // todo file types
            }
            properties.isFile() -> when (content) {
                is NodeContent.File -> content
                else -> NodeContent.Link
            }
            else -> NodeContent.Unknown
        }
        return copy(children = null, properties = properties, content = content)
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
                    child == null -> parse(path, line, root)
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
            else -> DirectoryType.Ordinary
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

    fun Node.delete(su: Boolean): Node {
        val output = Shell.exec(Shell[Shell.RM_RF].format(path), su)
        return when {
            output.success && error == null -> this
            output.success && error != null -> copy(error = null)
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
}