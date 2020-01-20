package ru.atomofiron.regextool.iss.service.model

import ru.atomofiron.regextool.utils.Shell
import java.io.File

class MutableXFile : XFile {
    companion object {
        private const val SLASH = '/'
        private const val ROOT = "/"
        private const val TOTAL = "total"
        private const val DIR_CHAR = 'd'
        private const val LINK_CHAR = 'l'

        var toyboxPath: String = ""
        private val parentSuffix = Regex("(?<=/)/*[^/]+/*$")
    }
    override var files: MutableList<MutableXFile>? = null
        set(value) {
            field = value
            isCaching = false
            isCacheActual = value != null
        }

    override var isOpened: Boolean = false
        private set(value) {
            require(isDirectory || !value) { "$completedPath is not a directory!" }
            field = value
        }

    var isCaching: Boolean = false
        private set
    override val isCached: Boolean get() = files != null
    override var isCacheActual: Boolean = false
        private set
    override var exists: Boolean = true
        private set

    override val completedPath: String by lazy {
        if (file.isDirectory && !file.absolutePath.endsWith(SLASH)) {
            file.absolutePath + SLASH
        } else {
            file.absolutePath
        }
    }

    override val completedParentPath: String by lazy { completedPath.replace(parentSuffix, "") }

    override val file: File
    override val access: String
    override val owner: String
    override val group: String
    override val size: String
    override val date: String
    override val time: String
    override val name: String
    override val suffix: String

    override val isDirectory: Boolean

    constructor(path: String) {
        this.file = File(path)
        access = ""
        owner = ""
        group = ""
        size = ""
        date = ""
        time = ""
        name = file.name
        suffix = ""
        isDirectory = file.isDirectory
    }

    constructor(parent: String, line: String) {
        val parts = line.split(Regex(" +"), 8)
        access = parts[0]
        owner = parts[2]
        group = parts[3]
        size = parts[4]
        date = parts[5]
        time = parts[6]
        name = parts[7]
        isDirectory = access[0] == DIR_CHAR
        if (parts[7].contains('>')) {
            // todo links
        }

        file = File(parent, parts[7])
        suffix = ""
    }

    fun open() {
        isOpened = true
    }

    fun close() {
        isOpened = false
    }

    fun clearChildren() {
        files!!.forEach { it.clear() }
    }

    /** @return error or null */
    fun updateCache(su: Boolean = false): String? {
        val oldFiles = files
        val error = cache(su)
        if (error != null) {
            return error
        }

        val newFiles = this.files!!
        if (oldFiles != null) {
            newFiles.forEachIndexed { newIndex, new ->
                if (new.isDirectory) {
                    val lastIndex = oldFiles.indexOf(new)
                    if (lastIndex != -1) {
                        newFiles[newIndex] = oldFiles[lastIndex]
                    }
                }
            }
        }
        return null
    }

    /** @return error or null */
    fun cache(su: Boolean = false): String? {
        if (isCaching) {
            return "Cache in process. $this"
        }
        isCaching = true
        //Thread.sleep(3000)
        require(isDirectory) { UnsupportedOperationException("$this is not a directory!") }
        val output = Shell.exec(Shell.LS_LAHL.format(toyboxPath, completedPath), su)
        return if (output.success) {
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
                        val file = MutableXFile(completedPath, lines[i])
                        if (file.isDirectory) {
                            dirs.add(file)
                        } else {
                            files.add(file)
                        }
                    }
                }
            }

            dirs.sortBy { it.name }
            files.sortBy { it.name }
            files.addAll(0, dirs)

            this.files = files
            null
        } else {
            files = ArrayList()
            output.error
        }
    }

    fun invalidateCache() {
        isCacheActual = false
    }

    private fun clear() {
        if (files == null) {
            return
        }
        files = null
        isOpened = false
    }

    override fun equals(other: Any?): Boolean {
        return when {
            other == null -> false
            other !is MutableXFile -> false
            other.isDirectory != isDirectory -> false
            else -> other.file.absolutePath == file.absolutePath
        }
    }

    override fun hashCode(): Int = completedPath.hashCode()

    override fun toString(): String = completedPath
}