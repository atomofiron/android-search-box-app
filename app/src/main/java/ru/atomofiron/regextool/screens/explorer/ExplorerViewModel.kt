package ru.atomofiron.regextool.screens.explorer

import android.app.Application
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import app.atomofiron.common.base.BaseRouter
import app.atomofiron.common.base.BaseViewModel
import app.atomofiron.common.util.LateinitLiveData
import app.atomofiron.common.util.SingleLiveEvent
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.injectable.service.explorer.model.XFile
import ru.atomofiron.regextool.model.ExplorerItemComposition
import ru.atomofiron.regextool.screens.explorer.places.XPlace
import ru.atomofiron.regextool.screens.explorer.sheet.BottomSheetMenuWithTitle.ExplorerItemOptions
import ru.atomofiron.regextool.screens.explorer.sheet.RenameDelegate.RenameData

class ExplorerViewModel(app: Application) : BaseViewModel<BaseRouter>(app) {

    // legacy field
    override val router = object : BaseRouter() { }

    override fun onViewDestroy() = Unit

    val directoryOptions = arrayListOf(R.id.menu_remove, R.id.menu_rename, R.id.menu_create)
    val oneFileOptions = arrayListOf(R.id.menu_remove, R.id.menu_rename)
    val manyFilesOptions = arrayListOf(R.id.menu_remove)

    val permissionRequiredWarning = SingleLiveEvent<Intent?>()
    val showOptions = SingleLiveEvent<ExplorerItemOptions>()
    val showCreate = SingleLiveEvent<XFile>()
    val showRename = SingleLiveEvent<RenameData>()
    val scrollToCurrentDir = SingleLiveEvent<Unit>()
    val historyDrawerGravity = MutableLiveData<Int>()
    val places = LateinitLiveData<List<XPlace>>()
    val itemComposition = LateinitLiveData<ExplorerItemComposition>()
    val items = MutableLiveData<List<XFile>>()
    val current = MutableLiveData<XFile?>()
    val notifyUpdate = SingleLiveEvent<XFile>()
    val notifyRemove = SingleLiveEvent<XFile>()
    val notifyInsert = SingleLiveEvent<Pair<XFile, XFile>>()
    val notifyUpdateRange = SingleLiveEvent<List<XFile>>()
    val notifyRemoveRange = SingleLiveEvent<List<XFile>>()
    val notifyInsertRange = SingleLiveEvent<Pair<XFile, List<XFile>>>()
}