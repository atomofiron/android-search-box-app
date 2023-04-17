package app.atomofiron.searchboxapp.screens.result.presenter

import app.atomofiron.searchboxapp.injectable.interactor.ResultInteractor
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.result.ResultRouter
import app.atomofiron.searchboxapp.screens.result.ResultViewState
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItem
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItemActionListener

class ResultItemActionDelegate(
    private val viewModel: ResultViewState,
    private val router: ResultRouter,
    private val curtainM: ResultCurtainMenuDelegate,
    private val interactor: ResultInteractor,
    private val preferenceStore: PreferenceStore,
) : ResultItemActionListener {
    override fun onItemClick(item: Node) {
        if (item.isFile && item.content is NodeContent.File.Text) {
            val task = viewModel.task.value
            router.openFile(item.path, task.uuid)
        } else {
            // todo open dir
        }
    }

    override fun onItemLongClick(item: Node) = viewModel.run {
        val options = when {
            checked.contains(item) -> ExplorerItemOptions(manyFilesOptions, checked, composition.value)
            else -> ExplorerItemOptions(oneFileOptions, listOf(item), composition.value)
        }
        curtainM.showOptions(options)
    }

    override fun onItemCheck(item: Node, isChecked: Boolean) {
        // todo item.isChecked = isChecked
        when {
            isChecked -> viewModel.checked.add(item)
            else -> viewModel.checked.remove(item)
        }
        viewModel.enableOptions.value = viewModel.checked.isNotEmpty()
    }

    override fun onItemVisible(item: ResultItem.Item) = interactor.cacheItem(item)
}