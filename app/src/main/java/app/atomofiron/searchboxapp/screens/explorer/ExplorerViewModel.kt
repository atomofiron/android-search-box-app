package app.atomofiron.searchboxapp.screens.explorer

import android.view.Gravity
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.flow.*
import app.atomofiron.common.util.property.WeakProperty
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.places.XPlace
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class ExplorerViewModel : BaseViewModel<ExplorerComponent, ExplorerFragment, ExplorerPresenter>() {

    val rootOptions = listOf(R.id.menu_create)
    val directoryOptions = listOf(R.id.menu_remove, R.id.menu_rename, R.id.menu_create)
    val oneFileOptions = listOf(R.id.menu_remove, R.id.menu_rename)
    val manyFilesOptions = listOf(R.id.menu_remove)

    val permissionRequiredWarning = ChannelFlow<Unit>()
    val scrollToCurrentDir = ChannelFlow<Unit>()
    val historyDrawerGravity = MutableStateFlow(Gravity.START)
    val places = MutableStateFlow<List<XPlace>>(listOf())
    val itemComposition = DeferredStateFlow<ExplorerItemComposition>()
    val items = MutableStateFlow<List<Node>>(listOf())
    val current = MutableStateFlow<Node?>(null)
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

    fun showPermissionRequiredWarning() {
        permissionRequiredWarning(viewModelScope)
    }

    fun scrollToCurrentDir() = scrollToCurrentDir.invoke(viewModelScope)
}