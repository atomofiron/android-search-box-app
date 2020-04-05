package ru.atomofiron.regextool.injectable.service.explorer

import android.content.SharedPreferences
import android.content.res.AssetManager
import kotlinx.coroutines.sync.withLock
import ru.atomofiron.regextool.injectable.service.explorer.model.MutableXFile
import ru.atomofiron.regextool.injectable.service.explorer.model.XFile
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.SettingsStore
import ru.atomofiron.regextool.log2
import ru.atomofiron.regextool.utils.Const

class ExplorerService constructor(
        assets: AssetManager,
        private val preferences: SharedPreferences,
        explorerStore: ExplorerStore,
        settingsStore: SettingsStore
) : PrivateExplorerServiceLogic(assets, explorerStore, settingsStore) {

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

    suspend fun openParent() {
        val dir = currentOpenedDir ?: return
        log2("openParent $dir")
        closeDir(dir)
    }

    override suspend fun updateItem(it: XFile) {
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
        val isNotEmptyOpenedDir = item.isDirectory && item.isOpened && dirFiles.isNotEmpty()
        when {
            item.isRoot && item.isChecked -> {
                if (item.isOpened && !uncheckAllChildren(item)) {
                    checkChildren(item)
                }
                item.isChecked = false
                explorerStore.notifyUpdate(item)
            }
            isNotEmptyOpenedDir && !item.isChecked -> {
                checkChildren(item)
                checked.remove(item)
            }
            isNotEmptyOpenedDir && item.isChecked -> when {
                uncheckAllChildren(item) -> {
                    item.isChecked = false
                    explorerStore.notifyUpdate(item)
                }
                else -> {
                    uncheckParent(item)
                    checked.add(item)
                }
            }
            item.isChecked -> {
                uncheckParent(item)
                checked.add(item)
            }
            !item.isChecked -> checked.remove(item)
        }

        explorerStore.notifyChecked()
    }

    suspend fun deleteItems(vararg items: XFile) {
        items.forEach {
            val item = findItem(it) ?: return@forEach
            deleteItem(item)
        }
    }
}