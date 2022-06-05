package app.atomofiron.searchboxapp.screens.viewer.presenter

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.Recipient
import app.atomofiron.common.util.flow.emitNow
import app.atomofiron.common.util.flow.value
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import app.atomofiron.searchboxapp.injectable.interactor.TextViewerInteractor
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import app.atomofiron.searchboxapp.screens.viewer.TextViewerRouter
import app.atomofiron.searchboxapp.screens.viewer.TextViewerViewModel
import app.atomofiron.searchboxapp.screens.viewer.presenter.curtain.CurtainSearchDelegate
import app.atomofiron.searchboxapp.utils.showCurtain

class SearchAdapterPresenterDelegate(
    private val viewModel: TextViewerViewModel,
    private val router: TextViewerRouter,
    private val interactor: TextViewerInteractor,
    preferenceStore: PreferenceStore,
    curtainChannel: CurtainChannel,
) : Recipient, FinderAdapterOutput {

    private val curtainDelegate = CurtainSearchDelegate(this)

    init {
        viewModel.run {
            uniqueItems.add(FinderStateItem.SearchAndReplaceItem())
            val characters = preferenceStore.specialCharacters.entity
            uniqueItems.add(FinderStateItem.SpecialCharactersItem(characters))
            uniqueItems.add(FinderStateItem.ConfigItem(isLocal = true))
            uniqueItems.add(FinderStateItem.TestItem())
            updateState(isLocal = true)
        }
        curtainChannel.flow.collectForMe(viewModel.viewModelScope) { controller ->
            curtainDelegate.set(viewModel.searchItems.value, viewModel.xFile, viewModel.composition)
            curtainDelegate.setController(controller)
        }
    }

    fun show() = router.showCurtain(recipient, R.layout.curtain_text_viewer_search)

    override fun onConfigChange(item: FinderStateItem.ConfigItem) = viewModel.updateConfig(item)

    override fun onCharacterClick(value: String) = viewModel.insertInQuery.emitNow(value)

    override fun onSearchChange(value: String) = viewModel.updateSearchQuery(value)

    override fun onSearchClick(value: String) {
        val config = viewModel.getUniqueItem(FinderStateItem.ConfigItem::class)
        interactor.search(value, config.ignoreCase, config.useRegex)
        curtainDelegate.controller?.close()
    }

    override fun onItemClick(item: FinderStateItem.ProgressItem) {
        if (item.finderTask.count > 0) {
            curtainDelegate.controller?.close()
            interactor.showTask(item.finderTask)
        }
    }

    override fun onProgressRemoveClick(item: FinderStateItem.ProgressItem) {
        interactor.removeTask(item.finderTask)
    }

    override fun onReplaceClick(value: String) = Unit

    override fun onProgressStopClick(item: FinderStateItem.ProgressItem) = Unit
}