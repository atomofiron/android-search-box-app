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
    private var currentOpenedDir: MutableXFile? = null

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
            updateClosedDir(root)

            notifyFiles()
            updates.notifyObservers(root)
        }
    }

    private fun notifyUpdate(file: XFile) {
        log("notifyUpdate $file")
        updates.notifyObservers(file)
    }

    private suspend fun notifyFiles() {
        log("notifyFiles")
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
        return null
    }

    fun persistState() {
        sp.edit().putString(Util.PREF_CURRENT_DIR, currentOpenedDir?.completedPath).apply()
    }

    suspend fun openDir(f: XFile) {
        val dir = findFile(f) ?: return
        if (!dir.isDirectory || !dir.isCached) {
            log("openDir return $dir")
            return
        }
        log("openDir $dir")
        val anotherDir = findOpenedDirIn(dir.completedParentPath)
        if (anotherDir != null) {
            log("openDir anotherDir != null $dir")
            anotherDir.close()
            anotherDir.clearChildren()
            removeAllChildren(anotherDir)
        }

        mutex.withLock {
            dir.open()
            currentOpenedDir = dir

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
        log("closeDir $dir")
        val parent = findFile(dir.completedParentPath)
        mutex.withLock {
            dir.close()
            currentOpenedDir = parent
        }

        dir.clearChildren()
        removeAllChildren(dir)
        notifyFiles()
    }

    suspend fun updateFile(f: XFile) {
        val file = findFile(f) ?: return
        log("updateFile $file")
        when {
            file == currentOpenedDir -> updateCurrentDir(file)
            file.isOpened -> Unit
            file.isDirectory -> updateClosedDir(file)
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
        if (dir != currentOpenedDir) {
            log("updateCurrentDir dir != currentOpenedDir $dir")
            return
        }
        if (dir.isCaching) {
            log("updateCurrentDir dir.isCaching $dir")
            return
        }
        log("updateCurrentDir $dir")
        val dirFiles = dir.files
        val su = useSu()
        val error = dir.updateCache(su)
        if (error != null) {
            log("updateCurrentDir error != null $dir")
            return
        }
        val newFiles = dir.files!!

        mutex.withLock {
            if (!dir.isOpened || !dir.isCached || dir != currentOpenedDir || !files.contains(dir)) {
                return
            }
            if (dirFiles != null) {
                files.removeAll(dirFiles)
            }

            if (newFiles.isNotEmpty()) {
                val index = files.indexOf(dir)
                files.addAll(index.inc(), newFiles)
            }
        }
        when {
            newFiles.size != dirFiles?.size -> notifyFiles()
            !dirFiles.containsAll(newFiles) -> notifyFiles()
            else -> log("updateCurrentDir dirFiles == newFiles")
        }
    }

    private fun updateClosedDir(dir: MutableXFile) {
        val su = useSu()
        require(dir.isDirectory) { IllegalArgumentException("Is not a directory! $dir") }
        if (dir.isOpened || dir.isCaching || dir.isCacheActual) {
            log("updateClosedDir $dir")
            return
        }
        log("updateClosedDir $dir")
        val dirFiles = dir.files
        val error = dir.cache(su)

        if (error != null) {
            log("updateClosedDir error != null ${dir.completedPath}\n$error")
            return
        }
        if (dir.files?.size != dirFiles?.size || dir.files != dirFiles) {
            notifyUpdate(dir)
        }
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