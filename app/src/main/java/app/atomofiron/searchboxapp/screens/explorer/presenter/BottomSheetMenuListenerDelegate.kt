package app.atomofiron.searchboxapp.screens.explorer.presenter

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.Recipient
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.value
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.explorer.ExplorerRouter
import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewModel
import app.atomofiron.searchboxapp.screens.explorer.sheet.RenameDelegate
import app.atomofiron.searchboxapp.utils.showCurtain

class BottomSheetMenuListenerDelegate(
    private val viewModel: ExplorerViewModel,
    private val router: ExplorerRouter,
    private val explorerStore: ExplorerStore,
    private val explorerInteractor: ExplorerInteractor,
    curtainChannel: CurtainChannel,
) : Recipient, MenuListener {

    private var options: ExplorerItemOptions? = null

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
            R.id.menu_create -> items.first().let { viewModel.showCreate.value = it }
            R.id.menu_rename -> {
                val item = items.first()
                val dirFiles = explorerStore.items
                        .find { it.root == item.root && it.completedPath == item.completedParentPath }
                        ?.children?.map { it.name }
                dirFiles ?: return
                val data = RenameDelegate.RenameData(viewModel.itemComposition.value, item, dirFiles)
                viewModel.showRename.value = data
            }
            R.id.menu_remove -> items.let(explorerInteractor::deleteItems)
        }
    }

    fun showOptions(options: ExplorerItemOptions) {
        this.options = options
        router.showCurtain(recipient, 0)
    }
}