package app.atomofiron.searchboxapp.screens.result.presenter

import app.atomofiron.common.util.flow.value
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.bottom_sheet_menu.BottomSheetMenuListener
import app.atomofiron.searchboxapp.injectable.interactor.ResultInteractor
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.finder.FinderResult
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.result.ResultViewModel

class BottomSheetMenuListenerDelegate(
    private val viewModel: ResultViewModel,
    private val interactor: ResultInteractor,
    appStore: AppStore,
) : BottomSheetMenuListener {
    private val resources by appStore.resourcesProperty

    private var items: List<XFile>? = null

    override fun onMenuItemSelected(id: Int) {
        val items = items ?: return
        when (id) {
            R.id.menu_copy_path -> {
                interactor.copyToClipboard(items.first() as FinderResult)
                viewModel.alerts.value = resources.getString(R.string.copied)
            }
            R.id.menu_remove -> interactor.deleteItems(items, viewModel.task.value.uuid)
        }
    }

    fun showOptions(options: ExplorerItemOptions) {
        items = options.items
        viewModel.showOptions.value = options
    }
}