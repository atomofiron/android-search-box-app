package app.atomofiron.searchboxapp.screens.result.presenter

import app.atomofiron.searchboxapp.injectable.interactor.ResultInteractor
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.finder.FinderResult
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.result.ResultRouter
import app.atomofiron.searchboxapp.screens.result.ResultViewModel
import app.atomofiron.searchboxapp.screens.result.adapter.FinderResultItem
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItemActionListener
import app.atomofiron.searchboxapp.utils.Util

class ResultItemActionDelegate(
        private val viewModel: ResultViewModel,
        private val router: ResultRouter,
        private val interactor: ResultInteractor,
        private val preferenceStore: PreferenceStore
) : ResultItemActionListener {
    override fun onItemClick(item: XFile) {
        item as FinderResult
        val textFormats = preferenceStore.textFormats.entity
        if (item.isFile && Util.isTextFile(item.completedPath, textFormats)) {
            val params = viewModel.task.value.params
            router.openFile(item.completedPath, params)
        } else {
            // todo open dir
        }
    }

    override fun onItemLongClick(item: XFile) {
        val options = if (viewModel.checked.contains(item)) {
            ExplorerItemOptions(viewModel.manyFilesOptions, viewModel.checked, viewModel.composition.value)
        } else {
            ExplorerItemOptions(viewModel.oneFileOptions, listOf(item), viewModel.composition.value)
        }
        viewModel.showOptions.value = options
    }

    override fun onItemCheck(item: XFile, isChecked: Boolean) {
        item as FinderResult
        item.isChecked = isChecked
        if (isChecked) {
            viewModel.checked.add(item)
        } else {
            viewModel.checked.remove(item)
        }
        viewModel.enableOptions.value = viewModel.checked.isNotEmpty()
    }

    override fun onItemVisible(item: FinderResultItem.Item) = interactor.cacheFile(item)
}