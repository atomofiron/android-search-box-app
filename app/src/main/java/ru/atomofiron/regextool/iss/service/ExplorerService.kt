package ru.atomofiron.regextool.iss.service

import android.preference.PreferenceManager
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.atomofiron.regextool.*
import ru.atomofiron.regextool.common.util.KObservable
import ru.atomofiron.regextool.iss.service.model.MutableXFile
import ru.atomofiron.regextool.iss.service.model.XFile
import ru.atomofiron.regextool.utils.Const.ROOT
import ru.atomofiron.regextool.utils.Shell
import java.io.File
import java.io.FileOutputStream
import kotlin.collections.ArrayList

class ExplorerService {
    private val sp = PreferenceManager.getDefaultSharedPreferences(App.context)

    var flag = false // todo remove
    val mutex = Mutex()
    private val files: MutableList<MutableXFile> = ArrayList()
        get() {
            require(!flag) { Exception("Oops") }
            return field
        }
    private val root = MutableXFile(File(sp.getString(Util.PREF_STORAGE_PATH, ROOT)))

    val store = KObservable<List<XFile>>(files)
    val updates = KObservable<XFile?>(null)

    init {
        flag = false
        GlobalScope.launch(Dispatchers.IO) {
            copyToybox()

            mutex.withLock {
                files.add(root)
                flag = false
            }
            cacheDir(root)

            notifyFiles()
            updates.notifyObservers(root)
        }
    }

    private suspend fun notifyFiles() {
        var items: List<XFile>? = null
        mutex.withLock {
            natik("one")
            items = ArrayList(files)
            flag = false
            natik("two ${items!!.size}")
        }
        store.notifyObservers(items!!)
    }

    private fun findFile(file: XFile): MutableXFile? {
        val target = files.find { it == file }
        if (target == null) {
            log("Target file ($file) was not found!")
        }
        return target
    }

    suspend fun openDir(file: XFile) {
        log("openDir $file")
        val dir = findFile(file) ?: return
        if (!dir.isDirectory) {
            return
        }
        if (dir.files == null) {
            log("WARNING $dir files is null")
            return
        }
        dir.open()
        if (dir.files!!.isNotEmpty()) {
            mutex.withLock {
                val index = files.indexOf(dir)
                flag = false
                files.addAll(index.inc(), dir.files!!)
                flag = false
            }
        }
        notifyFiles()
    }

    suspend fun updateDir(file: XFile) {
        log("updateDir ... $file")
        val dir = findFile(file) ?: return
        if (dir.isCaching) {
            log("isCaching return $dir")
            return
        }
        val su = sp.getBoolean(Util.PREF_USE_SU, false)
        val dirFiles = dir.files!!
        dir.cache(su)
        val newFiles = dir.files ?: return
        newFiles.forEachIndexed { index, new ->
            if (new.isDirectory) {
                val lastIndex = dirFiles.indexOf(new)
                if (lastIndex != -1) {
                    newFiles[index].files = dirFiles[lastIndex].files
                }
            }
        }

        mutex.withLock {
            if (!dir.isCached || !files.contains(dir)) {
                flag = false
                log("Oops, file lost! $dir")
                return
            }
            flag = false
            files.removeAll(dirFiles)
            flag = false

            if (dir.isOpened && dir.files!!.isEmpty()) {
                dir.close()
            } else {
                val index = files.indexOf(dir)
                flag = false
                files.addAll(index.inc(), dir.files!!)
                flag = false
            }
        }
        log("updateDir end...")
        if (dirFiles.size == newFiles.size || dirFiles.containsAll(newFiles)) {
            log("updateDir dirFiles == newFiles")
        } else {
            notifyFiles()
        }
    }

    suspend fun cacheDir(file: XFile) {
        val dir = findFile(file) ?: return
        if (dir.isOpened || !dir.isDirectory || dir.isCached) {
            log("cacheDir $file return")
            return
        }
        log("cacheDir $dir ...?")
        //Thread.sleep(1000)
        //log("SLEEP 1000 $this")
        dir.cache()
        log("cacheDir ok $dir")
        updates.notifyObservers(dir)
    }

    /*fun cacheChildrenDirs(dir: XFile, callback: (List<XFile>) -> Unit) {
        if (!dir.file.isDirectory || !dir.isOpened) {
            return
        }
        val dir = findFile(file) ?: return
        val su = sp.getBoolean(Util.PREF_USE_SU, false)
        dir.files!!.filter { it.file.isDirectory }.forEach { it.cache(su) }
        callback(files)
    }*/

    /*fun updateDirAndCacheChildren(dir: XFile, callback: (List<XFile>) -> Unit) {
        if (!dir.file.isDirectory || !dir.isOpened) {
            return
        }
        val dir = findFile(file) ?: return
        files.removeAll(dir.files!!)
        val su = sp.getBoolean(Util.PREF_USE_SU, false)
        dir.cacheChildren(su)
        val index = files.indexOf(dir)
        files.addAll(index.inc(), dir.files!!)
        log("cacheChildren end")
        callback(files)
    }*/

    suspend fun closeDir(file: XFile) {
        val dir = findFile(file) ?: return
        dir.close()

        removeAllChildren(dir)
        notifyFiles()

        dir.clearChildren()
    }

    suspend fun persistState() {
        mutex.withLock {
            val vertexes = files.filter { file ->
                file.isOpened && file.files?.find { child -> child.isOpened } == null
            }
            flag = false
        }
    }

    private suspend fun removeAllChildren(dir: XFile) {
        val path = dir.completedPath
        mutex.withLock {
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
            flag = false
        }
    }

    private fun copyToybox() {
        log("filesDir ${App.context.filesDir}")
        val toyboxPath = "${App.context.filesDir}/toybox"
        MutableXFile.toyboxPath = toyboxPath
        val toybox = File(toyboxPath)
        toybox.deleteRecursively()
        toybox.parentFile.mkdirs()
        val input = App.context.assets.open("toybox/toybox64")
        val bytes = input.readBytes()
        input.close()
        val output = FileOutputStream(toybox)
        output.write(bytes)
        output.close()
        Shell.exec(Shell.NATIVE_CHMOD_X.format(toyboxPath))
    }
}