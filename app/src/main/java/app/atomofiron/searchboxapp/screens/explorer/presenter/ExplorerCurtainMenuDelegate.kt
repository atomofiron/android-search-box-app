package app.atomofiron.searchboxapp.screens.explorer.presenter

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.Recipient
import app.atomofiron.common.util.flow.value
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.explorer.ExplorerRouter
import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewModel
import app.atomofiron.searchboxapp.screens.explorer.sheet.CurtainMenuWithTitle
import app.atomofiron.searchboxapp.screens.explorer.sheet.RenameDelegate
import app.atomofiron.searchboxapp.utils.showCurtain

class ExplorerCurtainMenuDelegate(
    private val viewModel: ExplorerViewModel,
    private val router: ExplorerRouter,
    private val explorerStore: ExplorerStore,
    private val explorerInteractor: ExplorerInteractor,
    curtainChannel: CurtainChannel,
) : Recipient, MenuListener {

    private val curtainAdapter = CurtainMenuWithTitle(R.menu.item_options_explorer, this)

    init {
        curtainChannel.flow.collectForMe(viewModel.viewModelScope, curtainAdapter::setController)
    }

    override fun onMenuItemSelected(id: Int) {
        val options = curtainAdapter.data ?: return
        curtainAdapter.controller?.close()
        val items = options.items
        when (id) {
            R.id.menu_create -> items.first().let { viewModel.showCreate.value = it }
            R.id.menu_rename -> {
                val item = items.first()
                val dirFiles = explorerStore.items
                    .find { it.isParentOf(item) }
                    ?.children?.map { it.name }
                dirFiles ?: return
                val data = RenameDelegate.RenameData(viewModel.itemComposition.value, item, dirFiles)
                viewModel.showRename.value = data
            }
            R.id.menu_remove -> items.let(explorerInteractor::deleteItems)
        }
    }

    fun showOptions(options: ExplorerItemOptions) {
        curtainAdapter.data = options
        router.showCurtain(recipient, 0)
    }
}