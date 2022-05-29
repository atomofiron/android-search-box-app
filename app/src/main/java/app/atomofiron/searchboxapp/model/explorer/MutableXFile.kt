package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.searchboxapp.sleep
import app.atomofiron.searchboxapp.utils.Shell
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MutableXFile : XFile {
    companion object {
        private const val SLASH = "/"
        private const val ROOT = "/"
        private const val TOTAL = "total"
        private const val DIR_CHAR = 'd'
        private const val LINK_CHAR = 'l'
        private const val FILE_CHAR = '-'
        private const val NO_SUCH_FILE = "ls: %s: No such file or directory\n"

        private const val UNKNOWN = -1

        private val spaces = Regex(" +")
        private val parentSuffix = Regex("(?<=/)/*[^/]+/*$")
        private val lastOneSlash = Regex("/*$")

        fun completePath(absolutePath: String, isDirectory: Boolean = true): String {
            return when {
                absolutePath == ROOT -> ROOT
                isDirectory -> absolutePath.replace(lastOneSlash, SLASH)
                else -> absolutePath
            }
        }

        fun completePathAsDir(absolutePath: String): String = completePath(absolutePath)

        fun create(parent: MutableXFile, name: String, isDirectory: Boolean, root: Int): MutableXFile {
            return MutableXFile("", "", "", "", "", "", name, "", isDirectory, parent.completedPath + name, root, parent)
        }

        fun asRoot(absolutePath: String): MutableXFile = byPath(absolutePath, asRoot = true)

        fun byPath(absolutePath: String): MutableXFile = byPath(absolutePath, asRoot = false)

        private fun byPath(absolutePath: String, asRoot: Boolean): MutableXFile {
            val file = File(absolutePath)
            return when {
                asRoot -> MutableXFile("", "", "", "", "", "", file.name, "", file.isDirectory, file.absolutePath, root = null)
                else -> MutableXFile("", "", "", "", "", "", file.name, "", file.isDirectory, file.absolutePath)
            }
        }

        private fun parse(completedParentPath: String, line: String, root: Int, parent: MutableXFile): MutableXFile {
            val parts = line.split(spaces, 8)
            val access = parts[0]
            val owner = parts[2]
            val group = parts[3]
            val size = parts[4]
            val date = parts[5]
            val time = parts[6]
            val name = parts[7]
            val isDirectory = access[0] == DIR_CHAR || parts[7] == ROOT

            if (parts[7].contains('>')) {
                // todo links
            }

            val suffix = ""
            val absolutePath = completedParentPath + name

            return MutableXFile(access, owner, group, size, date, time, name, suffix, isDirectory, absolutePath, root, parent)
        }
    }
    val parent: MutableXFile?

    override var children: MutableList<MutableXFile>? = null
        private set

    override var isOpened: Boolean = false
        private set(value) {
            require(isDirectory || !value) { "$completedPath is not a directory!" }
            field = value
        }

    private var dropCaching: Boolean = false
    private var isCaching: Boolean = false
    override val isCached: Boolean get() = children != null
    override var isCacheActual: Boolean = false
        private set

    override var completedPath: String private set
    override var completedParentPath: String private set

    override val mHashCode: Int

    override var access: String private set
    override var owner: String private set
    override var group: String private set
    override var size: String private set
    override var date: String private set
    override var time: String private set
    override val name: String
    override val suffix: String
    override var exists: Boolean = true; private set

    override var isDirectory: Boolean private set
    override var isFile: Boolean private set
    override val root: Int
    override val isRoot: Boolean
    override var isChecked: Boolean = false
    override var isDeleting: Boolean = false; private set

    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(
            access: String, owner: String, group: String, size: String, date: String, time: String,
            name: String, suffix: String, isDirectory: Boolean, absolutePath: String, root: Int? = 0, parent: MutableXFile? = null
    ) {
        this.access = access
        this.owner = owner
        this.group = group
        this.size = size
        this.date = date
        this.time = time
        this.name = name
        this.suffix = suffix
        this.isDirectory = isDirectory || absolutePath == ROOT
        this.isFile = !isDirectory && (access.isEmpty() || access[0] == FILE_CHAR)

        completedPath = completePath(absolutePath, isDirectory)
        completedParentPath = completedPath.replace(parentSuffix, "")

        this.parent = parent
        this.root = root ?: completedPath.hashCode()
        isRoot = root == null
        mHashCode = hashCode()
    }

    fun open() {
        isOpened = true
    }

    fun close() {
        isOpened = false
    }

    fun clear() {
        dropCaching = true
        isCacheActual = false
        children = null
        isOpened = false
    }

    fun clearChildren() {
        children!!.forEach {
            it.clear()
            it.isChecked = false
        }
    }

    fun invalidateCache() {
        isCacheActual = false
    }

    fun replace(item: MutableXFile, with: MutableXFile): String? {
        val files = children
        val index = files?.indexOf(item)
        if (index == null || index == UNKNOWN) {
            return "Replacing failed. $this"
        }
        files.removeAt(index)
        files.add(index, with)
        return null
    }

    fun add(item: MutableXFile): String? {
        val files = children
        files ?: return "Addition failed. $this"
        files.add(0, item)
        return null
    }

    /**
     *  @return error or null or "" if nothing changed
     *  todo try FileObserver
     */
    fun updateCache(su: Boolean, completely: Boolean = false): String? {
        dropCaching = false
        when {
            !exists -> return "Item does not exist. $this"
            isDeleting -> return "Item is deleting. $this"
            isCaching -> return "Cache in process. $this"
            isCacheActual -> return "Cache is actual. $this"
        }
        return when {
            completely -> {
                var error = cacheAsFile(su)
                if (error.isNullOrEmpty() && isDirectory) {
                    error = cacheAsDir(su)
                }
                error
            }
            isDirectory -> cacheAsDir(su)
            else -> cacheAsFile(su)
        }
    }

    fun willBeDeleted() {
        isDeleting = true
    }

    fun delete(su: Boolean): String? {
        isDeleting = true
        sleep(1000)
        var error: String? = null
        val output = Shell.exec(Shell[Shell.RM_RF].format(completedPath), su)
        when (output.success) {
            true -> exists = false
            else -> error = output.error
        }
        isDeleting = false
        return error
    }

    fun rename(name: String, su: Boolean): Pair<String?, MutableXFile?> {
        val absolutePath = completedParentPath + name
        val root = if (isRoot) null else root
        val item = MutableXFile(access, owner, group, size, date, time, name, suffix, isDirectory, absolutePath, root, parent)
        item.children = children
        item.isOpened = isOpened
        item.isCacheActual = isCacheActual
        item.isChecked = isChecked
        val output = Shell.exec(Shell[Shell.MV].format(completedPath, item.completedPath), su)
        return when {
            output.success -> {
                // do not update dir's files
                item.cacheAsFile(su)
                Pair(null, item)
            }
            else -> Pair(output.error, null)
        }
    }

    fun create(su: Boolean): String? {
        val output = when {
            isDirectory -> Shell.exec(Shell[Shell.MKDIR].format(completedPath), su)
            else -> Shell.exec(Shell[Shell.TOUCH].format(completedPath), su)
        }
        return if (output.success) null else output.error
    }

    private fun cacheAsDir(su: Boolean): String? {
        isCaching = true
        sleep((Math.random() * 300).toLong())
        val output = Shell.exec(Shell[Shell.LS_LAHL].format(completedPath), su)

        if (dropCaching) {
            isCaching = false
            return "Dir was cleared. $this"
        }
        // todo ls: /storage/AA83-2C7F//.android_secure: Permission denied
        if (!output.success && parseDoesExists(output.error)) {
            children = ArrayList()
            isCaching = false
            isCacheActual = true
            return output.error
        }
        val lines = output.output.split("\n")
        val dirs = ArrayList<MutableXFile>()
        val files = ArrayList<MutableXFile>()

        if (lines.isNotEmpty()) {
            var start = 0
            if (lines[start].startsWith(TOTAL)) {
                start++
            }
            for (i in start until lines.size) {
                if (lines[i].isNotEmpty()) {
                    val file = parse(completedPath, lines[i], root, this)
                    if (file.isDirectory) {
                        dirs.add(file)
                    } else {
                        files.add(file)
                    }
                }
            }
        }

        dirs.sortBy { it.name.toLowerCase(Locale.ROOT) }
        files.sortBy { it.name.toLowerCase(Locale.ROOT) }
        files.addAll(0, dirs)

        val oldFiles = this.children
        this.children = files
        persistOldFiles(oldFiles)
        isCaching = false
        isCacheActual = true
        return null
    }

    private fun persistOldFiles(oldFiles: MutableList<MutableXFile>?) {
        val newFiles = this.children!!
        if (oldFiles != null) {
            newFiles.forEachIndexed { newIndex, new ->
                val lastIndex = oldFiles.indexOf(new)
                if (lastIndex != -1) {
                    val last = oldFiles[lastIndex]
                    last.updateData(new)
                    newFiles[newIndex] = last
                }
            }
        }
    }

    private fun cacheAsFile(su: Boolean): String? {
        isCaching = true
        val output = Shell.exec(Shell[Shell.LS_LAHLD].format(completedPath), su)
        val line = output.output.replace("\n", "")
        var wasChanged = true
        if (output.success && line.isNotEmpty()) {
            wasChanged = !compareAttributes(line)
            updateAttributes(line)
        }
        isCaching = false
        isCacheActual = true

        return when {
            !output.success -> {
                parseDoesExists(output.error)
                output.error
            }
            wasChanged -> null
            else -> ""
        }
    }

    private fun compareAttributes(line: String): Boolean {
        val parts = line.split(spaces, 8)
        if (access != parts[0]) return false
        if (owner != parts[2]) return false
        if (group != parts[3]) return false
        if (size != parts[4]) return false
        if (date != parts[5]) return false
        if (time != parts[6]) return false
        if (isDirectory != (access[0] == DIR_CHAR)) return false
        if (isFile != (!isDirectory && access[0] == FILE_CHAR)) return false
        return true
    }

    private fun updateAttributes(line: String) {
        val parts = line.split(spaces, 8)
        access = parts[0]
        owner = parts[2]
        group = parts[3]
        size = parts[4]
        date = parts[5]
        time = parts[6]
        isDirectory = access[0] == DIR_CHAR
        isFile = !isDirectory && access[0] == FILE_CHAR
    }

    private fun parseDoesExists(error: String): Boolean {
        if (error == NO_SUCH_FILE.format(completedPath)) {
            exists = false
        }
        return !exists
    }

    private fun updateData(file: MutableXFile) {
        access = file.access
        owner = file.owner
        group = file.group
        size = file.size
        date = file.date
        time = file.time
        isDirectory = file.isDirectory
        isFile = !isDirectory && (access.isEmpty() || access[0] == FILE_CHAR)
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            !is MutableXFile -> false
            else -> other.mHashCode == mHashCode
        }
    }

    override fun hashCode(): Int = completedPath.hashCode() + root + (if (isDirectory) 1 else 0)

    override fun toString(): String = completedPath
}