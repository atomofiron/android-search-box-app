package app.atomofiron.searchboxapp.screens.explorer.presenter

import app.atomofiron.common.util.flow.value
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.bottom_sheet_menu.BottomSheetMenuListener
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewModel
import app.atomofiron.searchboxapp.screens.explorer.sheet.RenameDelegate

class BottomSheetMenuListenerDelegate(
    private val viewModel: ExplorerViewModel,
    private val explorerStore: ExplorerStore,
    private val explorerInteractor: ExplorerInteractor
) : BottomSheetMenuListener {

    private var items: List<XFile>? = null

    override fun onMenuItemSelected(id: Int) {
        val items = items ?: return
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
        items = options.items
        viewModel.showOptions.value = options
    }
}