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

    val mutex = Mutex()
    private val files: MutableList<MutableXFile> = ArrayList()
    private val root = MutableXFile(sp.getString(Util.PREF_STORAGE_PATH, ROOT)!!)

    val store = KObservable<List<XFile>>(files)
    val updates = KObservable<XFile?>(null)

    private fun useSu() = sp.getBoolean(Util.PREF_USE_SU, false)

    init {
        GlobalScope.launch(Dispatchers.IO) {
            copyToybox()

            mutex.withLock {
                files.add(root)
            }
            cacheDir(root)

            notifyFiles()
            updates.notifyObservers(root)
        }
    }

    private suspend fun notifyFiles() {
        var items: List<XFile>? = null
        mutex.withLock {
            items = ArrayList(files)
        }
        store.notifyObservers(items!!)
    }

    private fun findFile(file: XFile): MutableXFile? {
        val files = files
        var i = 0
        // ConcurrentModificationException
        while (i < files.size) {
            val f = files[i++]
            if (file == f) {
                return f
            }
        }
        log("Target file was not found! $file")
        return null
    }

    suspend fun openDir(file: XFile) {
        log("openDir $file")
        val dir = findFile(file) ?: return
        if (!dir.isDirectory) {
            return
        }
        if (dir.files == null) {
            log("FUCK files is null $dir")
            return
        }
        dir.open()
        if (dir.files!!.isNotEmpty()) {
            mutex.withLock {
                val index = files.indexOf(dir)
                files.addAll(index.inc(), dir.files!!)
            }
        }
        notifyFiles()
    }

    suspend fun updateDir(file: XFile) {
        log("updateDir ... $file")
        val dir = findFile(file) ?: return
        val su = useSu()
        if (dir.isCaching) {
            log("isCaching return $dir")
            return
        }
        val dirFiles = dir.files
        dir.cache(su)
        val newFiles = dir.files ?: return
        if (dirFiles != null) {
            newFiles.forEachIndexed { index, new ->
                if (new.isDirectory) {
                    val lastIndex = dirFiles.indexOf(new)
                    if (lastIndex != -1) {
                        newFiles[index].files = dirFiles[lastIndex].files
                    }
                }
            }
        }

        mutex.withLock {
            if (!dir.isCached || !files.contains(dir)) {
                log("Oops, file lost! $dir")
                return
            }
            if (dirFiles != null) {
                files.removeAll(dirFiles)
            }

            if (dir.isOpened && dir.files!!.isEmpty()) {
                dir.close()
            } else {
                val index = files.indexOf(dir)
                files.addAll(index.inc(), dir.files!!)
            }
        }
        log("updateDir end...")
        when {
            newFiles.size != dirFiles?.size -> notifyFiles()
            !dirFiles.containsAll(newFiles) -> notifyFiles()
            else -> log("updateDir dirFiles == newFiles")
        }
    }

    fun cacheDir(file: XFile) {
        val dir = findFile(file) ?: return
        val su = useSu()
        if (dir.isOpened || !dir.isDirectory || dir.isCached) {
            log("cacheDir $file return")
            return
        }
        log("cacheDir $dir ...?")
        //Thread.sleep(1000)
        //log("SLEEP 1000 $this")
        dir.cache(su)
        log("cacheDir ok $dir")
        updates.notifyObservers(dir)
    }

    suspend fun closeDir(file: XFile) {
        val dir = findFile(file) ?: return
        dir.close()

        removeAllChildren(dir)
        notifyFiles()

        dir.clearChildren()
    }

    fun persistState() {
    }

    private suspend fun removeAllChildren(dir: XFile) {
        val path = dir.completedPath
        mutex.withLock {
            val each = files.iterator()
            var removed = false
            loop@ while (each.hasNext()) {
                val next = each.next()
                when {
                    next.completedPath == ROOT -> Unit
                    next.completedParentPath.startsWith(path) -> {
                        each.remove()
                        removed = true
                    }
                    removed -> break@loop
                }
            }
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