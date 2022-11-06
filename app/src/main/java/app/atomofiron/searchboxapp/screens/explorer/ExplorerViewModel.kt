package app.atomofiron.searchboxapp.screens.explorer

import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.dataFlow
import app.atomofiron.common.util.flow.value
import app.atomofiron.common.util.property.WeakProperty
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeError
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
    val items = dataFlow<List<Node>>()
    val current = dataFlow<Node?>()
    val alerts = ChannelFlow<NodeError>()

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

    fun onChanged(items: List<Node>) {
        viewModelScope.launch {
            this@ExplorerViewModel.items.value = items
        }
    }

    fun onChanged(item: Node?) {
        viewModelScope.launch {
            current.value = item
        }
    }

    fun alert(value: NodeError) {

    }
}