package ru.atomofiron.regextool.injectable.service.explorer

import android.content.SharedPreferences
import android.content.res.AssetManager
import ru.atomofiron.regextool.injectable.service.explorer.model.MutableXFile
import ru.atomofiron.regextool.injectable.service.explorer.model.XFile
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.log2
import ru.atomofiron.regextool.utils.Const

class ExplorerService constructor(
        assets: AssetManager,
        private val preferences: SharedPreferences,
        explorerStore: ExplorerStore,
        preferenceStore: PreferenceStore
) : PrivateExplorerServiceLogic(assets, explorerStore, preferenceStore) {

    fun persistState() {
        preferences.edit().putString(Const.PREF_CURRENT_DIR, currentOpenedDir?.completedPath).apply()
    }

    suspend fun setRoots(vararg path: String) {
        val roots = path.map { MutableXFile.byPath(it) }
        log2("setRoots ${roots.size}")
        super.setRoots(roots)
    }

    fun invalidateDir(dir: XFile) {
        val item = findItem(dir)
        item ?: return log2("invalidateDir $item")
        super.invalidateDir(item)
    }

    suspend fun open(it: XFile) {
        val item = findItem(it)
        item ?: return log2("open $item")
        super.open(item)
    }

    suspend fun openParent() {
        val dir = currentOpenedDir
        dir ?: return log2("openParent $dir")
        super.closeDir(dir)
    }

    suspend fun updateItem(it: XFile) {
        val item = findItem(it)
        item ?: return log2("updateItem $item")
        super.updateItem(item)
    }

    fun checkItem(it: XFile, isChecked: Boolean) {
        val item = findItem(it)
        item ?: return log2("checkItem $isChecked $item")
        super.checkItem(item, isChecked)
    }

    suspend fun deleteItems(vararg its: XFile) {
        log2("deleteItems ${its.size}")
        val items = its.mapNotNull { findItem(it) }
        super.deleteItems(items)
    }

    suspend fun rename(it: XFile, name: String) {
        val item = findItem(it)
        item ?: return log2("rename $name $item")
        super.rename(item, name)
    }

    suspend fun create(it: XFile, name: String, directory: Boolean) {
        val dir = findItem(it)
        dir ?: return log2("create $name $dir")
        super.create(dir, name, directory)
    }
}