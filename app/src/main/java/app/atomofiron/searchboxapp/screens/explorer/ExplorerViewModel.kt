package app.atomofiron.searchboxapp.screens.explorer

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.flow.LiveDataFlow
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.model.explorer.Change
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.places.XPlace
import app.atomofiron.searchboxapp.screens.explorer.sheet.RenameDelegate.RenameData

class ExplorerViewModel : BaseViewModel<ExplorerComponent, ExplorerFragment>() {

    val rootOptions = listOf(R.id.menu_create)
    val directoryOptions = listOf(R.id.menu_remove, R.id.menu_rename, R.id.menu_create)
    val oneFileOptions = listOf(R.id.menu_remove, R.id.menu_rename)
    val manyFilesOptions = listOf(R.id.menu_remove)

    val permissionRequiredWarning = LiveDataFlow(Unit, single = true)
    val showOptions = LiveDataFlow<ExplorerItemOptions>(single = true)
    val showCreate = LiveDataFlow<XFile>(single = true)
    val showRename = LiveDataFlow<RenameData>(single = true)
    val scrollToCurrentDir = LiveDataFlow(Unit, single = true)
    val historyDrawerGravity = LiveDataFlow<Int>()
    val places = LiveDataFlow<List<XPlace>>()
    val itemComposition = LiveDataFlow<ExplorerItemComposition>()
    val items = LiveDataFlow<List<XFile>>()
    val current = LiveDataFlow<XFile?>()
    val notifyUpdate = LiveDataFlow<XFile>(single = true)
    val notifyRemove = LiveDataFlow<XFile>(single = true)
    val notifyInsert = LiveDataFlow<Pair<XFile, XFile>>(single = true)
    val notifyUpdateRange = LiveDataFlow<List<XFile>>(single = true)
    val notifyRemoveRange = LiveDataFlow<List<XFile>>(single = true)
    val notifyInsertRange = LiveDataFlow<Pair<XFile, List<XFile>>>(single = true)

    override val component = DaggerExplorerComponent
            .builder()
            .bind(viewProperty)
            .bind(viewModelScope)
            .bind(this)
            .dependencies(DaggerInjector.appComponent)
            .build()

    override fun inject(view: ExplorerFragment) {
        super.inject(view)
        component.inject(this)
        component.inject(view)
    }

    fun onChanged(items: List<XFile>) {
        viewModelScope.launch {
            this@ExplorerViewModel.items.value = items
        }
    }

    fun onChanged(change: Change) {
        viewModelScope.launch {
            when (change) {
                is Change.Update -> notifyUpdate.value = change.item
                is Change.Remove -> notifyRemove.value = change.item
                is Change.Insert -> notifyInsert.value = Pair(change.previous, change.item)
                is Change.UpdateRange -> notifyUpdateRange.value = change.items
                is Change.RemoveRange -> notifyRemoveRange.value = change.items
                is Change.InsertRange -> notifyInsertRange.value = Pair(change.previous, change.items)
            }
        }
    }

    fun onChanged(item: XFile?) {
        viewModelScope.launch {
            current.value = item
        }
    }
}