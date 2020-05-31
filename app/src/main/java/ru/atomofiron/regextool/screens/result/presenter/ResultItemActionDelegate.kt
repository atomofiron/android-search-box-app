package ru.atomofiron.regextool.screens.result.presenter

import ru.atomofiron.regextool.injectable.interactor.ResultInteractor
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.model.finder.FinderResult
import ru.atomofiron.regextool.model.other.ExplorerItemOptions
import ru.atomofiron.regextool.screens.result.ResultRouter
import ru.atomofiron.regextool.screens.result.ResultViewModel
import ru.atomofiron.regextool.screens.result.adapter.FinderResultItem
import ru.atomofiron.regextool.screens.result.adapter.ResultItemActionListener
import ru.atomofiron.regextool.utils.Util

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
            router.openFile(item.completedPath, item.finderQueryParams)
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
        viewModel.showOptions.invoke(options)
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