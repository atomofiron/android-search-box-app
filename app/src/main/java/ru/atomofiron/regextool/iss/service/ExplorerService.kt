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
    // todo smart update
    private var currentDir: MutableXFile? = null

    val store = KObservable<List<XFile>>(files)
    val updates = KObservable<XFile?>(null)

    // todo make storage
    private fun useSu() = sp.getBoolean(Util.PREF_USE_SU, false)

    init {
        GlobalScope.launch(Dispatchers.IO) {
            copyToybox()

            mutex.withLock {
                files.add(root)
            }
            updateDir(root)

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

    private fun findFile(f: XFile): MutableXFile? = findFile(f.completedPath)

    private fun findFile(completedPath: String): MutableXFile? {
        val files = files
        var i = 0
        // ConcurrentModificationException
        while (i < files.size) {
            val file = files[i++]
            if (file.completedPath == completedPath) {
                return file
            }
        }
        log("Target file was not found! $completedPath")
        return null
    }

    private fun findOpenedDirIn(completedParentPath: String): MutableXFile? {
        val files = files
        var i = 0
        // ConcurrentModificationException
        while (i < files.size) {
            val file = files[i++]
            if (file.isOpened && file.completedParentPath == completedParentPath && file.completedPath != ROOT) {
                return file
            }
        }
        log("Opened subdirectory was not found! $completedParentPath")
        return null
    }

    fun persistState() {
        sp.edit().putString(Util.PREF_CURRENT_DIR, currentDir?.completedPath).apply()
    }

    suspend fun openDir(f: XFile) {
        log("openDir $f")
        val dir = findFile(f) ?: return
        if (!dir.isDirectory || !dir.isCached) {
            log("openDir return $f")
            return
        }
        val anotherDir = findOpenedDirIn(dir.completedParentPath)
        if (anotherDir != null) {
            log("Force close $anotherDir")
            anotherDir.close()
            anotherDir.clearChildren()
            removeAllChildren(anotherDir)
        }

        mutex.withLock {
            dir.open()
            currentDir = dir

            val dirFiles = dir.files
            if (dirFiles?.isNotEmpty() == true) {
                val index = files.indexOf(dir)
                files.addAll(index.inc(), dirFiles)
            }
        }
        notifyFiles()
    }

    suspend fun closeDir(f: XFile) {
        val dir = findFile(f) ?: return
        val parent = findFile(dir.completedParentPath)
        mutex.withLock {
            dir.close()
            currentDir = parent
        }

        dir.clearChildren()
        removeAllChildren(dir)
        notifyFiles()
    }

    suspend fun updateFile(f: XFile) {
        val file = findFile(f) ?: return
        when {
            file == currentDir -> updateCurrentDir(file)
            file.isDirectory -> updateDir(file)
            else -> return updateFile(file)
        }
    }

    private fun updateFile(file: MutableXFile) {
        // todo check file exists
    }

    fun invalidateDir(f: XFile) {
        findFile(f)?.invalidateCache()
    }

    private suspend fun updateCurrentDir(dir: MutableXFile) {
        if (dir != currentDir) {
            return
        }
        log("updateCurrentDir")
        if (dir.isCaching) {
            log("isCaching return $dir")
            return
        }
        val dirFiles = dir.files
        val su = useSu()
        val error = dir.cache(su)
        if (error != null) {
            log("updateDir: $error")
            return
        }
        val newFiles = dir.files!!
        if (dirFiles != null) {
            newFiles.forEachIndexed { newIndex, new ->
                if (new.isDirectory) {
                    val lastIndex = dirFiles.indexOf(new)
                    if (lastIndex != -1) {
                        newFiles[newIndex] = dirFiles[lastIndex]
                    }
                }
            }
        }

        mutex.withLock {
            if (!dir.isOpened || !dir.isCached || dir != currentDir || !files.contains(dir)) {
                log("Oops, (parent) current dir closed?")
                return
            }
            if (dirFiles != null) {
                files.removeAll(dirFiles)
            }

            if (dir.files!!.isEmpty()) {
                dir.close()
            } else {
                val index = files.indexOf(dir)
                files.addAll(index.inc(), dir.files!!)
            }
        }
        log("updateOpenedDir end...")
        when {
            newFiles.size != dirFiles?.size -> notifyFiles()
            !dirFiles.containsAll(newFiles) -> notifyFiles()
            else -> log("updateDir dirFiles == newFiles")
        }
    }

    private fun updateDir(dir: MutableXFile) {
        val su = useSu()
        require(dir.isDirectory) { IllegalArgumentException("Is not a directory! $dir") }
        if (dir.isOpened || dir.isCaching || dir.isCacheActual) {
            log("updateDir return $dir")
            return
        }
        log("updateClosedDir $dir")
        val dirFiles = dir.files
        val error = dir.cache(su)

        if (error != null) {
            log("cacheDir: $error")
            return
        }
        updates.notifyObservers(dir)
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