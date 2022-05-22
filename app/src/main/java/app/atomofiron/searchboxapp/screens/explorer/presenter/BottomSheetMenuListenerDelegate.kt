package app.atomofiron.searchboxapp.screens.explorer.presenter

import app.atomofiron.common.util.flow.value
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.bottom_sheet_menu.BottomSheetMenuListener
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewModel
import app.atomofiron.searchboxapp.screens.explorer.sheet.RenameDelegate

class BottomSheetMenuListenerDelegate(
    private val viewModel: ExplorerViewModel,
    private val explorerStore: ExplorerStore,
    private val explorerInteractor: ExplorerInteractor
) : BottomSheetMenuListener {

    override fun onMenuItemSelected(id: Int) {
        when (id) {
            R.id.menu_create -> viewModel.showOptions.value.items[0].let { viewModel.showCreate.value = it }
            R.id.menu_rename -> {
                val item = viewModel.showOptions.value.items[0]
                item ?: return
                val dirFiles = explorerStore.items
                        .find { it.root == item.root && it.completedPath == item.completedParentPath }
                        ?.children?.map { it.name }
                dirFiles ?: return
                val data = RenameDelegate.RenameData(viewModel.itemComposition.value, item, dirFiles)
                viewModel.showRename.value = data
            }
            R.id.menu_remove -> viewModel.showOptions.value.items.let(explorerInteractor::deleteItems)
        }
    }
}