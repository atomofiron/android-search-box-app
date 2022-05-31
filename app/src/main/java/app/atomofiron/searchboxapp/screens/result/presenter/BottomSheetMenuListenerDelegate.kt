package app.atomofiron.searchboxapp.screens.result.presenter

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.Recipient
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.value
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import app.atomofiron.searchboxapp.injectable.interactor.ResultInteractor
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.model.finder.FinderResult
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.result.ResultRouter
import app.atomofiron.searchboxapp.screens.result.ResultViewModel
import app.atomofiron.searchboxapp.utils.showCurtain

class BottomSheetMenuListenerDelegate(
    private val viewModel: ResultViewModel,
    private val router: ResultRouter,
    private val interactor: ResultInteractor,
    appStore: AppStore,
    curtainChannel: CurtainChannel,
) : Recipient, MenuListener {

    private var options: ExplorerItemOptions? = null
    private val resources by appStore.resourcesProperty

    init {
        curtainChannel.flow.filterForMe().collect(viewModel.viewModelScope) { controller ->
            val options = options
            when {
                controller == null -> Unit
                options == null -> controller.close(immediately = true)
                else -> viewModel.showOptions.value = Pair(options, controller)
            }
        }
    }

    override fun onMenuItemSelected(id: Int) {
        val options = options ?: return
        val items = options.items
        when (id) {
            R.id.menu_copy_path -> {
                interactor.copyToClipboard(items.first() as FinderResult)
                viewModel.alerts.value = resources.getString(R.string.copied)
            }
            R.id.menu_remove -> interactor.deleteItems(items, viewModel.task.value.uuid)
        }
    }

    fun showOptions(options: ExplorerItemOptions) {
        this.options = options
        router.showCurtain(recipient, 0)
    }
}