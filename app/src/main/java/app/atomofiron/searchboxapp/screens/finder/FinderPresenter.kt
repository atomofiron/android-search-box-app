package app.atomofiron.searchboxapp.screens.finder

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.collect
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.FinderStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.presenter.FinderAdapterPresenterDelegate
import kotlinx.coroutines.CoroutineScope

class FinderPresenter(
    scope: CoroutineScope,
    private val viewState: FinderViewState,
    router: FinderRouter,
    private val finderAdapterDelegate: FinderAdapterPresenterDelegate,
    private val explorerStore: ExplorerStore,
    private val preferenceStore: PreferenceStore,
    private val finderStore: FinderStore,
    private val preferenceChannel: PreferenceChannel
) : BasePresenter<FinderViewModel, FinderRouter>(scope, router),
        FinderAdapterOutput by finderAdapterDelegate
{
    private val uniqueItems: MutableList<FinderStateItem> get() = viewState.uniqueItems
    private val progressItems: MutableList<FinderStateItem.ProgressItem> get() = viewState.progressItems

    init {
        uniqueItems.add(FinderStateItem.SearchAndReplaceItem())
        uniqueItems.add(FinderStateItem.SpecialCharactersItem(arrayOf()))
        uniqueItems.add(FinderStateItem.TestItem())
        uniqueItems.add(FinderStateItem.ButtonsItem)

        finderStore.tasks.forEach {
            progressItems.add(FinderStateItem.ProgressItem(it))
        }

        onSubscribeData()
        viewState.switchConfigItemVisibility()
    }

    override fun onSubscribeData() {
        preferenceStore.excludeDirs.collect(scope) { excludeDirs ->
            viewState.setExcludeDirsValue(excludeDirs)
            viewState.updateState()
        }
        preferenceStore.dockGravity.collect(scope) { gravity ->
            viewState.historyDrawerGravity.value = gravity
        }
        preferenceStore.specialCharacters.collect(scope) { chs ->
            viewState.updateUniqueItem(FinderStateItem.SpecialCharactersItem(chs))
        }
        viewState.reloadHistory.collect(scope) {
            preferenceChannel.notifyHistoryImported()
        }

        explorerStore.current.collect(scope) {
            val checked = explorerStore.searchTargets.value
            if (checked.isEmpty()) {
                scope.launch {
                    viewState.updateTargets(it, checked)
                }
            }
        }
        explorerStore.searchTargets.collect(scope) {
            val currentDir = explorerStore.current.value
            scope.launch {
                viewState.updateTargets(currentDir, it)
            }
        }
        finderStore.notifications.collect(scope) {
            scope.launch {
                viewState.onFinderTaskUpdate(it)
            }
        }
    }

    fun onDockGravityChange(gravity: Int) = preferenceStore { setDockGravity(gravity) }

    fun onExplorerOptionSelected() = router.showExplorer()

    fun onConfigOptionSelected() = viewState.switchConfigItemVisibility()

    fun onSettingsOptionSelected() = router.showSettings()

    fun onHistoryItemClick(node: String) = viewState.replaceQuery(node)
}