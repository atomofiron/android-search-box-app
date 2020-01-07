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
        dir as MutableXFile
        dir.open()
        if (dir.files!!.isNotEmpty()) {
            val index = files.indexOf(dir)
            files.addAll(index.inc(), dir.files!!)
            callback(files)
        }
    }

    fun updateDir(dir: XFile) {
        dir as MutableXFile
        val su = sp.getBoolean(Util.PREF_USE_SU, false)
        val dirFiles = dir.files!!
        dir.cache(su)
        dir.files!!.forEachIndexed { index, it ->
            val i = dirFiles.indexOf(it)
            val isNew = i == -1
            when {
                isNew && it.file.isDirectory -> it.cache(su)
                it.file.isDirectory -> dir.files!![index] = dirFiles[i]
            }
        }

        files.removeAll(dirFiles)

        if (dir.opened && dir.files!!.isEmpty()) {
            dir.close()
        } else {
            val index = files.indexOf(dir)
            files.addAll(index + 1, dir.files!!)
        }
    }

    fun cacheChildrenDirs(dir: XFile, callback: (List<XFile>) -> Unit) {
        if (!dir.file.isDirectory || !dir.opened) {
            return
        }
        dir as MutableXFile
        val su = sp.getBoolean(Util.PREF_USE_SU, false)
        dir.files!!.filter { it.file.isDirectory }.forEach { it.cache(su) }
        callback(files)
    }

    fun closeDir(dir: XFile, callback: (List<XFile>) -> Unit) {
        dir as MutableXFile
        dir.close()
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
}