package app.atomofiron.searchboxapp.screens.viewer.presenter

import app.atomofiron.common.arch.Recipient
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import app.atomofiron.searchboxapp.injectable.interactor.TextViewerInteractor
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import app.atomofiron.searchboxapp.screens.viewer.TextViewerRouter
import app.atomofiron.searchboxapp.screens.viewer.TextViewerViewState
import app.atomofiron.searchboxapp.screens.viewer.presenter.curtain.CurtainSearchDelegate
import app.atomofiron.searchboxapp.utils.showCurtain
import kotlinx.coroutines.CoroutineScope

class SearchAdapterPresenterDelegate(
    private val scope: CoroutineScope,
    private val viewState: TextViewerViewState,
    private val router: TextViewerRouter,
    private val interactor: TextViewerInteractor,
    preferenceStore: PreferenceStore,
    curtainChannel: CurtainChannel,
) : Recipient, FinderAdapterOutput {

    private val curtainDelegate = CurtainSearchDelegate(this)

    init {
        viewState.run {
            uniqueItems.add(FinderStateItem.SearchAndReplaceItem())
            val characters = preferenceStore.specialCharacters.value
            uniqueItems.add(FinderStateItem.SpecialCharactersItem(characters))
            uniqueItems.add(FinderStateItem.ConfigItem(isLocal = true))
            uniqueItems.add(FinderStateItem.TestItem())
            updateState(isLocal = true)
        }
        curtainChannel.flow.collectForMe(scope) { controller ->
            curtainDelegate.set(viewState.searchItems.value, viewState.item, viewState.composition)
            curtainDelegate.setController(controller)
        }
    }

    fun show() = router.showCurtain(recipient, R.layout.curtain_text_viewer_search)

    override fun onConfigChange(item: FinderStateItem.ConfigItem) = viewState.updateConfig(item)

    override fun onCharacterClick(value: String) = viewState.sendInsertInQuery(value)

    override fun onSearchChange(value: String) = viewState.updateSearchQuery(value)

    override fun onSearchClick(value: String) {
        val config = viewState.getUniqueItem(FinderStateItem.ConfigItem::class)
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