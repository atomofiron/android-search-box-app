package app.atomofiron.searchboxapp.screens.explorer

import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.flow.sharedFlow
import app.atomofiron.common.util.flow.value
import app.atomofiron.common.util.property.WeakProperty
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.model.explorer.Change
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.places.XPlace
import app.atomofiron.searchboxapp.screens.explorer.sheet.RenameDelegate.RenameData
import javax.inject.Inject

class ExplorerViewModel : BaseViewModel<ExplorerComponent, ExplorerFragment, ExplorerPresenter>() {

    val rootOptions = listOf(R.id.menu_create)
    val directoryOptions = listOf(R.id.menu_remove, R.id.menu_rename, R.id.menu_create)
    val oneFileOptions = listOf(R.id.menu_remove, R.id.menu_rename)
    val manyFilesOptions = listOf(R.id.menu_remove)

    val permissionRequiredWarning = sharedFlow(Unit, single = true)
    val showOptions = sharedFlow<ExplorerItemOptions>(single = true)
    val showCreate = sharedFlow<XFile>(single = true)
    val showRename = sharedFlow<RenameData>(single = true)
    val scrollToCurrentDir = sharedFlow(Unit, single = true)
    val historyDrawerGravity = sharedFlow<Int>()
    val places = sharedFlow<List<XPlace>>()
    val itemComposition = sharedFlow<ExplorerItemComposition>()
    val items = sharedFlow<List<XFile>>()
    val current = sharedFlow<XFile?>()
    val notifyUpdate = sharedFlow<XFile>(single = true)
    val notifyRemove = sharedFlow<XFile>(single = true)
    val notifyInsert = sharedFlow<Pair<XFile, XFile>>(single = true)
    val notifyUpdateRange = sharedFlow<List<XFile>>(single = true)
    val notifyRemoveRange = sharedFlow<List<XFile>>(single = true)
    val notifyInsertRange = sharedFlow<Pair<XFile, List<XFile>>>(single = true)
    val alerts = sharedFlow<String>(single = true)

    @Inject
    override lateinit var presenter: ExplorerPresenter

    override fun createComponent(fragmentProperty: WeakProperty<Fragment>) = DaggerExplorerComponent
        .builder()
        .bind(fragmentProperty)
        .bind(viewModelScope)
        .bind(this)
        .dependencies(DaggerInjector.appComponent)
        .build()

    override fun inject(view: ExplorerFragment) {
        super.inject(view)

        component.inject(this)
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