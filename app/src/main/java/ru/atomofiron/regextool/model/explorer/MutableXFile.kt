package ru.atomofiron.regextool.model.explorer

import ru.atomofiron.regextool.App
import ru.atomofiron.regextool.sleep
import ru.atomofiron.regextool.utils.Shell
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

        private val toyboxPath: String by lazy { App.pathToybox }
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

        fun create(completedParentPath: String, name: String, isDirectory: Boolean, root: Int): MutableXFile {
            return MutableXFile("", "", "", "", "", "", name, "", isDirectory, completedParentPath + name, root)
        }

        fun byPath(absolutePath: String): MutableXFile {
            val file = File(absolutePath)
            return MutableXFile("", "", "", "", "", "", file.name, "", file.isDirectory, file.absolutePath, root = null)
        }

        private fun parse(completedParentPath: String, line: String, root: Int): MutableXFile {
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

            return MutableXFile(access, owner, group, size, date, time, name, suffix, isDirectory, absolutePath, root)
        }
    }
    override var files: MutableList<MutableXFile>? = null
        private set

    override var isOpened: Boolean = false
        private set(value) {
            require(isDirectory || !value) { "$completedPath is not a directory!" }
            field = value
        }

    private var dropCaching: Boolean = false
    private var isCaching: Boolean = false
    override val isCached: Boolean get() = files != null
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

    constructor(
            access: String, owner: String, group: String, size: String, date: String, time: String,
            name: String, suffix: String, isDirectory: Boolean, absolutePath: String, root: Int
    ) : this(access, owner, group, size, date, time, name, suffix, isDirectory, absolutePath, root as Int?)

    private constructor(
            access: String, owner: String, group: String, size: String, date: String, time: String,
            name: String, suffix: String, isDirectory: Boolean, absolutePath: String, root: Int?
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
        files = null
        isOpened = false
    }

    fun clearChildren() {
        files!!.forEach {
            it.clear()
            it.isChecked = false
        }
    }

    fun invalidateCache() {
        isCacheActual = false
    }

    fun replace(item: MutableXFile, with: MutableXFile): String? {
        val files = files
        val index = files?.indexOf(item)
        if (index == null || index == UNKNOWN) {
            return "Replacing failed. $this"
        }
        files.removeAt(index)
        files.add(index, with)
        return null
    }

    fun add(item: MutableXFile): String? {
        val files = files
        files ?: return "Addition failed. $this"
        files.add(0, item)
        return null
    }

    /** @return error or null */
    fun updateCache(su: Boolean): String? {
        dropCaching = false
        when {
            !exists -> return "Item does not exist. $this"
            isDeleting -> return "Item is deleting. $this"
            isCaching -> return "Cache in process. $this"
            isCacheActual -> return "Cache is actual. $this"
        }
        return when {
            isDirectory -> cacheAsDir(su)
            else -> cacheAsFile(su)
        }
    }

    fun delete(su: Boolean = false): String? {
        isDeleting = true
        var error: String? = null
        val output = Shell.exec(Shell.RM_RF.format(toyboxPath, completedPath), su)
        when (output.success) {
            true -> exists = false
            else -> error = output.error
        }
        isDeleting = false
        return error
    }

    fun rename(name: String, su: Boolean = false): Pair<String?, MutableXFile?> {
        val absolutePath = completedParentPath + name
        val root = if (isRoot) null else root
        val item = MutableXFile(access, owner, group, size, date, time, name, suffix, isDirectory, absolutePath, root)
        item.files = files
        item.isOpened = isOpened
        item.isCacheActual = isCacheActual
        item.isChecked = isChecked
        val output = Shell.exec(Shell.MV.format(toyboxPath, completedPath, item.completedPath), su)
        return when {
            output.success -> {
                // do not update dir's files
                item.cacheAsFile()
                Pair(null, item)
            }
            else -> Pair(output.error, null)
        }
    }

    fun create(su: Boolean = false): String? {
        val output = when {
            isDirectory -> Shell.exec(Shell.MKDIR.format(toyboxPath, completedPath), su)
            else -> Shell.exec(Shell.TOUCH.format(toyboxPath, completedPath), su)
        }
        return if (output.success) null else output.error
    }

    private fun cacheAsDir(su: Boolean = false): String? {
        isCaching = true
        sleep((Math.random() * 300).toLong())
        val output = Shell.exec(Shell.LS_LAHL.format(toyboxPath, completedPath), su)
        return when {
            dropCaching -> {
                isCaching = false
                return "Dir was cleared. $this"
            }
            output.success -> {
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
                            val file = parse(completedPath, lines[i], root)
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

                val oldFiles = this.files
                this.files = files
                persistOldFiles(oldFiles)
                isCaching = false
                isCacheActual = true
                null
            }
            else -> {
                parseDoesExists(output.error)
                files = ArrayList()
                isCaching = false
                isCacheActual = true
                output.error
            }
        }
    }

    private fun persistOldFiles(oldFiles: MutableList<MutableXFile>?) {
        val newFiles = this.files!!
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

    private fun cacheAsFile(su: Boolean = false): String? {
        isCaching = true
        val output = Shell.exec(Shell.LS_LAHL.format(toyboxPath, completedPath), su)
        val line = output.output.replace("\n", "")
        if (output.success && line.isNotEmpty()) {
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
        isCaching = false
        isCacheActual = true

        return when (output.success) {
            true -> null
            false -> {
                parseDoesExists(output.error)
                output.error
            }
        }
    }

    private fun parseDoesExists(error: String) {
        if (error == NO_SUCH_FILE.format(completedPath)) {
            exists = false
        }
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