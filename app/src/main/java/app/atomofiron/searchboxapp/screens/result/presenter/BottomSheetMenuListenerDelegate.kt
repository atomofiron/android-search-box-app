package app.atomofiron.searchboxapp.screens.result.presenter

import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.bottom_sheet_menu.BottomSheetMenuListener
import app.atomofiron.searchboxapp.injectable.interactor.ResultInteractor
import app.atomofiron.searchboxapp.model.finder.FinderResult
import app.atomofiron.searchboxapp.screens.result.ResultViewModel

class BottomSheetMenuListenerDelegate(
        private val viewModel: ResultViewModel,
        private val interactor: ResultInteractor
) : BottomSheetMenuListener {

    override fun onMenuItemSelected(id: Int) {
        val item = viewModel.showOptions.data!!.items[0]
        when (id) {
            R.id.menu_copy_path -> {
                interactor.copyToClipboard(item as FinderResult)
                viewModel.alerts.invoke(viewModel.context.getString(R.string.copied))
            }
            R.id.menu_remove -> interactor.deleteItems(listOf(item), viewModel.task.value.uuid)
        }
    }
}