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
        log2("notifyUpdate $file")
        updates.notifyObservers(file)
    }

    private suspend fun notifyFiles() {
        log2("notifyFiles")
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
            log2("openDir return $dir")
            return
        }
        when {
            dir == currentOpenedDir -> closeDir(dir)
            dir.isOpened -> reopenDir(dir)
            else -> openDir(dir)
        }
    }

    private suspend fun reopenDir(dir: MutableXFile) {
        log2("reopenDir $dir")
        val anotherDir = findOpenedDirIn(dir.completedPath)
        if (anotherDir != null) {
            log2("reopenDir anotherDir != null $anotherDir")
            anotherDir.close()
            anotherDir.clearChildren()
            currentOpenedDir = dir

            removeAllChildren(anotherDir)
            notifyFiles()
        } else {
            closeDir(dir)
        }
    }

    private suspend fun openDir(dir: MutableXFile) {
        require(dir.isDirectory) { IllegalArgumentException("Is not a directory! $dir") }
        log2("openDir $dir")
        val anotherDir = findOpenedDirIn(dir.completedParentPath)
        if (anotherDir != null) {
            log2("openDir anotherDir != null $dir")
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
        log2("closeDir $dir")
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
        log2("updateFile $file")
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
            log2("updateCurrentDir dir != currentOpenedDir $dir")
            return
        }
        if (dir.isCaching || dir.isCacheActual) {
            log2("updateCurrentDir dir.isCaching $dir")
            return
        }
        log2("updateCurrentDir $dir")
        val dirFiles = dir.files
        val su = useSu()
        val error = dir.updateCache(su)
        if (error != null) {
            log2("updateCurrentDir error != null $dir")
            return
        }
        val newFiles = dir.files!!

        mutex.withLock {
            if (!dir.isCached || !files.contains(dir)) {
                log2("updateCurrentDir return $dir")
                return
            }
            if (!dir.isOpened) {
                log2("updateCurrentDir isOpened $dir")
                return notifyUpdate(dir)
            }
            if (dir != currentOpenedDir) {
                log2("updateCurrentDir !isCurrentOpenedDir $dir")
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
            else -> log2("updateCurrentDir dirFiles == newFiles")
        }
    }

    private fun updateClosedDir(dir: MutableXFile) {
        val su = useSu()
        require(dir.isDirectory) { IllegalArgumentException("Is not a directory! $dir") }
        if (dir.isOpened || dir.isCaching || dir.isCacheActual) {
            log2("updateClosedDir return ${dir.isCaching} || ${dir.isCacheActual} $dir")
            return
        }
        log2("updateClosedDir $dir")
        val error = dir.updateCache(su)

        if (error != null) {
            log2("updateClosedDir error != null ${dir.completedPath}\n$error")
        }
        notifyUpdate(dir)
        /*
        нельзя не уведомлять, потому что:
        1 у дочерних папок isCached=true, updateCache(), isCaching=true
        2 родительская папка закрывается, у дочерних папок clear(), files=null, isCached=false
        3 родительская папка открывается
        4 дочерние папки кешироваться не начинают, потому что isCaching=true
        5 список отображает папки как isCached=false
        5 по окончании кеширования может быть oldFiles==newFiles
         */
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