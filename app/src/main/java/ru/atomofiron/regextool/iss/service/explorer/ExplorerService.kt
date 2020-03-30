package ru.atomofiron.regextool.iss.service.explorer

import android.content.SharedPreferences
import kotlinx.coroutines.sync.withLock
import ru.atomofiron.regextool.iss.service.explorer.model.MutableXFile
import ru.atomofiron.regextool.iss.service.explorer.model.XFile
import ru.atomofiron.regextool.iss.store.ExplorerStore
import ru.atomofiron.regextool.iss.store.SettingsStore
import ru.atomofiron.regextool.log2
import ru.atomofiron.regextool.utils.Const

class ExplorerService(
        private val preferences: SharedPreferences,
        explorerStore: ExplorerStore,
        settingsStore: SettingsStore
) : PrivateExplorerServiceLogic(explorerStore, settingsStore) {

    fun persistState() {
        preferences.edit().putString(Const.PREF_CURRENT_DIR, currentOpenedDir?.completedPath).apply()
    }

    suspend fun setRoots(vararg path: String) {
        val roots = path.map { MutableXFile.byPath(it) }

        mutex.withLock {
            // todo remove old root and children
            files.clear()
            files.addAll(roots)
        }
        explorerStore.notifyItems()
        roots.forEach { updateClosedDir(it) }
    }

    fun invalidateDir(dir: XFile) {
        val item = findItem(dir) ?: return
        invalidateDir(item)
    }

    suspend fun openDir(d: XFile) {
        val dir = findItem(d) ?: return
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

    suspend fun updateItem(it: XFile) {
        val file = findItem(it) ?: return
        log2("updateItem $file")
        when {
            file.isOpened && file == currentOpenedDir -> updateCurrentDir(file)
            file.isOpened -> Unit
            file.isDirectory -> updateClosedDir(file)
            else -> return updateFile(file)
        }
    }

    fun checkItem(it: XFile, isChecked: Boolean) {
        val item = findItem(it) ?: return
        log2("checkItem $isChecked $it")
        item.isChecked = isChecked

        val dirFiles = item.files ?: arrayListOf()
        val isNotEmptyOpenedDir = dirFiles.isNotEmpty() && item.isDirectory && item.isOpened
        when {
            isNotEmptyOpenedDir && !item.isChecked -> {
                checked.remove(item)
                checkChildren(item)
            }
            isNotEmptyOpenedDir && item.isChecked -> if (!uncheckChildren(item)) {
                checked.add(item)
            }
            item.isChecked -> {
                checked.add(item)
                uncheckParent(item)
            }
            !item.isChecked -> checked.remove(item)
        }

        explorerStore.notifyChecked()
    }
}