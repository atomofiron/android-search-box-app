package ru.atomofiron.regextool.screens.explorer.presenter

import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.bottom_sheet_menu.BottomSheetMenuListener
import ru.atomofiron.regextool.injectable.interactor.ExplorerInteractor
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.screens.explorer.ExplorerViewModel
import ru.atomofiron.regextool.screens.explorer.sheet.RenameDelegate

class BottomSheetMenuListenerDelegate(
        private val viewModel: ExplorerViewModel,
        private val explorerStore: ExplorerStore,
        private val explorerInteractor: ExplorerInteractor
) : BottomSheetMenuListener {

    override fun onMenuItemSelected(id: Int) {
        when (id) {
            R.id.menu_create -> viewModel.showOptions.data?.items?.get(0)?.let(viewModel.showCreate::invoke)
            R.id.menu_rename -> {
                val item = viewModel.showOptions.data?.items?.get(0)
                item ?: return
                val dirFiles = explorerStore.items
                        .find { it.root == item.root && it.completedPath == item.completedParentPath }
                        ?.children?.map { it.name }
                dirFiles ?: return
                val data = RenameDelegate.RenameData(viewModel.itemComposition.value, item, dirFiles)
                viewModel.showRename.invoke(data)
            }
            R.id.menu_remove -> viewModel.showOptions.data?.items?.let(explorerInteractor::deleteItems)
        }
    }
}