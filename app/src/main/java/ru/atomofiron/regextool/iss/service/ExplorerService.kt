package ru.atomofiron.regextool.iss.service

import android.preference.PreferenceManager
import ru.atomofiron.regextool.App
import ru.atomofiron.regextool.Util
import ru.atomofiron.regextool.iss.service.model.MutableXFile
import ru.atomofiron.regextool.iss.service.model.XFile
import java.io.File
import kotlin.collections.ArrayList

class ExplorerService {
    private val files: MutableList<MutableXFile> = ArrayList()
    private val root = MutableXFile(File("/sdcard/"))

    private val sp = PreferenceManager.getDefaultSharedPreferences(App.context)

    init {
        files.add(root)
        val su = sp.getBoolean(Util.PREF_USE_SU, false)

        root.cache(su)
    }

    fun getFiles(): List<XFile> = files

    fun openDir(dir: XFile, callback: (List<XFile>) -> Unit) {
        if (dir.file.isFile) {
            return
        }
        val cursor = findCursor(dir)
        val su = sp.getBoolean(Util.PREF_USE_SU, false)
        cursor.open(su)
        val index = files.indexOf(cursor)
        files.addAll(index + 1, cursor.files!!)
        callback(files)
    }

    fun cacheChildrenDirs(dir: XFile) {
        if (!dir.file.isDirectory) {
            return
        }
        val cursor = findCursor(dir)
        val su = sp.getBoolean(Util.PREF_USE_SU, false)
        cursor.files!!.filter { it.file.isDirectory }.forEach { it.cache(su) }
    }

    fun closeDir(dir: XFile, callback: (List<XFile>) -> Unit) {
        val cursor = findCursor(dir)
        cursor.close()
        removeAllChildren(dir)

        callback(files)
    }

    fun persistState() {
        val vertexes = files.filter { file ->
            file.opened && file.files?.find { child -> child.opened } == null
        }
    }

    private fun removeAllChildren(dir: XFile) {
        val path = dir.completedPath
        val each = files.iterator()
        var removed = false
        while (each.hasNext()) {
            val next = each.next()
            if (next.completedParentPath.startsWith(path)) {
                each.remove()
                removed = true
            } else if (removed) {
                break
            }
        }
    }

    private fun findCursor(dir: XFile): MutableXFile {
        val path = dir.completedPath
        var cursor = root

        while (cursor.completedPath != path) {
            cursor.files!!.forEach {
                if (path.startsWith(it.completedPath)) {
                    cursor = it
                    return@forEach
                }
            }
        }
        return cursor
    }
}