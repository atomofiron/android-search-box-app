package ru.atomofiron.regextool.iss.service.model

import ru.atomofiron.regextool.log
import ru.atomofiron.regextool.utils.Shell
import java.io.File

class MutableXFile : XFile {
    companion object {
        private const val TOTAL = "total"
    }
    override var files: List<MutableXFile>? = null
    private set

    override var opened: Boolean = false
    private set(value) {
        require(file.isDirectory || !value) { "$completedPath is not a directory!" }
        field = value
    }

    override val completedPath: String by lazy {
        if (file.isFile || file.absolutePath.endsWith("/")) {
            file.absolutePath
        } else {
            file.absolutePath + "/"
        }
    }

    override val completedParentPath: String by lazy {
        if (file.parentFile.absolutePath.endsWith("/")) {
            file.parentFile.absolutePath
        } else {
            file.parentFile.absolutePath + "/"
        }
    }

    override val file: File
    override val access: String
    override val owner: String
    override val group: String
    override val size: String
    override val date: String
    override val time: String
    override val name: String

    constructor(file: File) {
        this.file = file
        access = ""
        owner = ""
        group = ""
        size = ""
        date = ""
        time = ""
        name = file.name
    }

    constructor(parent: String, line: String) {
        val parts = line.split(Regex(" +"), 8)
        file = File(parent, parts[7])
        access = parts[0]
        owner = parts[2]
        group = parts[3]
        size = parts[4]
        date = parts[5]
        time = parts[6]
        name = parts[7]
    }

    /** @return error or null */
    fun open(su: Boolean = false): String? {
        val error = when (files) {
            null -> {
                // second chance to cache
                cache(su).also {
                    if (it != null) {
                        files = ArrayList()
                    }
                }
            }
            else -> null
        }
        opened = files!!.isNotEmpty()
        return error
    }

    fun close() {
        files!!.forEach { it.clear() }
        opened = false
    }

    /** @return error or null */
    fun cache(su: Boolean = false): String? {
        val output = Shell.exec("ls -lah \"$completedPath\"", su)
        return if (output.success) {
            val lines = output.output.split("\n")
            val dirs = ArrayList<MutableXFile>()
            val files = ArrayList<MutableXFile>()

            if (lines.isNotEmpty()) {
                val first = if (lines[0].startsWith(TOTAL)) 1 else 0
                for (i in first until lines.size) {
                    if (lines[i].isNotEmpty() && !lines[i].endsWith(" ..") && !lines[i].endsWith(" .")) {
                        val file = MutableXFile(completedPath, lines[i])
                        if (file.file.isDirectory) {
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
            log("output.error $completedPath ${output.error}")
            output.error
        }
    }

    private fun clear() {
        files = null
        opened = false
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            null -> false
            !is MutableXFile -> false
            else -> other.file.absolutePath == file.absolutePath
        }
    }

    override fun hashCode(): Int = completedPath.hashCode()

    override fun toString(): String = completedPath
}