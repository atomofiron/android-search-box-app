package ru.atomofiron.regextool.screens.result.presenter

import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.model.finder.FinderResult
import ru.atomofiron.regextool.model.other.ExplorerItemOptions
import ru.atomofiron.regextool.screens.explorer.adapter.util.ExplorerItemBinder
import ru.atomofiron.regextool.screens.result.ResultViewModel

class ResultItemActionDelegate(private val viewModel: ResultViewModel) : ExplorerItemBinder.ExplorerItemBinderActionListener {
    override fun onItemClick(item: XFile) {
    }

    override fun onItemLongClick(item: XFile) {
        val options = if (viewModel.checked.contains(item)) {
            ExplorerItemOptions(viewModel.manyFilesOptions, viewModel.checked, viewModel.composition.value)
        } else {
            ExplorerItemOptions(viewModel.oneFileOptions, arrayListOf(item), viewModel.composition.value)
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
}