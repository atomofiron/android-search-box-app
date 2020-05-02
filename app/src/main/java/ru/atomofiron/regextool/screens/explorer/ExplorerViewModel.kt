package ru.atomofiron.regextool.screens.explorer

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.LateinitLiveData
import app.atomofiron.common.util.SingleLiveEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.di.DaggerInjector
import ru.atomofiron.regextool.model.explorer.Change
import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.model.other.ExplorerItemOptions
import ru.atomofiron.regextool.model.preference.ExplorerItemComposition
import ru.atomofiron.regextool.screens.explorer.places.XPlace
import ru.atomofiron.regextool.screens.explorer.sheet.RenameDelegate.RenameData
import javax.inject.Inject

class ExplorerViewModel : BaseViewModel<ExplorerComponent, ExplorerFragment>() {

    @Inject
    lateinit var scope: CoroutineScope

    val rootOptions = listOf(R.id.menu_create)
    val directoryOptions = listOf(R.id.menu_remove, R.id.menu_rename, R.id.menu_create)
    val oneFileOptions = listOf(R.id.menu_remove, R.id.menu_rename)
    val manyFilesOptions = listOf(R.id.menu_remove)

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

    override val component = DaggerExplorerComponent
            .builder()
            .bind(viewProperty)
            .bind(this)
            .dependencies(DaggerInjector.appComponent)
            .build()

    override fun inject(view: ExplorerFragment) {
        super.inject(view)
        component.inject(this)
        component.inject(view)
    }

    fun onChanged(items: List<XFile>) {
        scope.launch {
            this@ExplorerViewModel.items.value = items
        }
    }

    fun onChanged(change: Change) {
        scope.launch {
            when (change) {
                is Change.Update -> notifyUpdate(change.item)
                is Change.Remove -> notifyRemove(change.item)
                is Change.Insert -> notifyInsert(Pair(change.previous, change.item))
                is Change.UpdateRange -> notifyUpdateRange(change.items)
                is Change.RemoveRange -> notifyRemoveRange(change.items)
                is Change.InsertRange -> notifyInsertRange(Pair(change.previous, change.items))
            }
        }
    }

    fun onChanged(item: XFile?) {
        scope.launch {
            current.value = item
        }
    }
}