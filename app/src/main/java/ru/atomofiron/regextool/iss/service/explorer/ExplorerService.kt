package ru.atomofiron.regextool.iss.service.explorer

import kotlinx.coroutines.sync.withLock
import ru.atomofiron.regextool.iss.service.explorer.model.MutableXFile
import ru.atomofiron.regextool.iss.service.explorer.model.XFile
import ru.atomofiron.regextool.log2
import ru.atomofiron.regextool.utils.Const

class ExplorerService : PrivateExplorerServiceLogic() {

    fun persistState() {
        sp.edit().putString(Const.PREF_CURRENT_DIR, currentOpenedDir?.completedPath).apply()
    }

    suspend fun setRoots(vararg path: String) {
        val roots = path.map { MutableXFile.byPath(it) }

        mutex.withLock {
            // todo remove old root and children
            files.clear()
            files.addAll(roots)
        }
        publisher.notifyFiles()
        roots.forEach { updateClosedDir(it) }
    }

    fun invalidateDir(f: XFile) {
        val dir = findFile(f) ?: return
        invalidateDir(dir)
    }

    suspend fun openDir(f: XFile) {
        val dir = findFile(f) ?: return
        if (!dir.isDirectory) {
            log2("openDir return $dir")
            return
        }
        if (!dir.isCached) {
            return updateClosedDir(dir)
        }
        when {
            dir == currentOpenedDir -> closeDir(dir)
            dir.isOpened -> reopenDir(dir)
            else -> openDir(dir)
        }
    }

    suspend fun updateFile(f: XFile) {
        val file = findFile(f) ?: return
        log2("updateFile $file")
        when {
            file.isOpened && file == currentOpenedDir -> updateCurrentDir(file)
            file.isOpened -> Unit
            file.isDirectory -> updateClosedDir(file)
            else -> return updateTheFile(file)
        }
    }
}