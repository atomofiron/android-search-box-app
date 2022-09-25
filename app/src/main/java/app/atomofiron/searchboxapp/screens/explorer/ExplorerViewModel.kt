package app.atomofiron.searchboxapp.screens.explorer

import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.flow.dataFlow
import app.atomofiron.common.util.flow.value
import app.atomofiron.common.util.property.WeakProperty
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.model.explorer.Change
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.places.XPlace
import javax.inject.Inject

class ExplorerViewModel : BaseViewModel<ExplorerComponent, ExplorerFragment, ExplorerPresenter>() {

    val rootOptions = listOf(R.id.menu_create)
    val directoryOptions = listOf(R.id.menu_remove, R.id.menu_rename, R.id.menu_create)
    val oneFileOptions = listOf(R.id.menu_remove, R.id.menu_rename)
    val manyFilesOptions = listOf(R.id.menu_remove)

    val permissionRequiredWarning = dataFlow(Unit, single = true)
    val scrollToCurrentDir = dataFlow(Unit, single = true)
    val historyDrawerGravity = dataFlow<Int>()
    val places = dataFlow<List<XPlace>>()
    val itemComposition = dataFlow<ExplorerItemComposition>()
    val items = dataFlow<List<XFile>>()
    val current = dataFlow<XFile?>()
    val notifyUpdate = dataFlow<XFile>(single = true)
    val notifyRemove = dataFlow<XFile>(single = true)
    val notifyInsert = dataFlow<Pair<XFile, XFile>>(single = true)
    val notifyUpdateRange = dataFlow<List<XFile>>(single = true)
    val notifyRemoveRange = dataFlow<List<XFile>>(single = true)
    val notifyInsertRange = dataFlow<Pair<XFile, List<XFile>>>(single = true)
    val alerts = dataFlow<String>(single = true)

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
                is Change.Nothing -> Unit
            }
        }
    }

    fun onChanged(item: XFile?) {
        viewModelScope.launch {
            current.value = item
        }
    }
}