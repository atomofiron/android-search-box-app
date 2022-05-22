package app.atomofiron.searchboxapp.screens.finder

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.emitNow
import app.atomofiron.common.util.flow.value
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.FinderStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.presenter.FinderAdapterPresenterDelegate

class FinderPresenter(
    viewModel: FinderViewModel,
    router: FinderRouter,
    private val finderAdapterDelegate: FinderAdapterPresenterDelegate,
    private val explorerStore: ExplorerStore,
    private val preferenceStore: PreferenceStore,
    private val finderStore: FinderStore,
    private val preferenceChannel: PreferenceChannel
) : BasePresenter<FinderViewModel, FinderRouter>(viewModel, router),
        FinderAdapterOutput by finderAdapterDelegate
{
    private val uniqueItems: MutableList<FinderStateItem> get() = viewModel.uniqueItems
    private val progressItems: MutableList<FinderStateItem.ProgressItem> get() = viewModel.progressItems

    init {
        uniqueItems.add(FinderStateItem.SearchAndReplaceItem())
        val characters = preferenceStore.specialCharacters.entity
        uniqueItems.add(FinderStateItem.SpecialCharactersItem(characters))
        uniqueItems.add(FinderStateItem.TestItem())

        finderStore.tasks.forEach {
            progressItems.add(FinderStateItem.ProgressItem(it))
        }

        val excludeDirs = preferenceStore.excludeDirs.value
        viewModel.setExcludeDirsValue(excludeDirs)
        viewModel.updateState()
        onSubscribeData()
        viewModel.switchConfigItemVisibility()
    }

    override fun onSubscribeData() {
        preferenceStore.dockGravity.collect(scope) { gravity ->
            viewModel.historyDrawerGravity.value = gravity
        }
        preferenceStore.specialCharacters.collect(scope) { chs ->
            viewModel.updateUniqueItem(FinderStateItem.SpecialCharactersItem(chs))
        }
        scope.launch {
            viewModel.apply {
                preferenceChannel.apply {
                    reloadHistory.collect(historyImportedEvent::emit)
                }
            }
        }

        explorerStore.current.collect(scope) {
            val checked = explorerStore.storeChecked.value
            if (checked.isEmpty()) {
                scope.launch {
                    viewModel.updateTargets(it, checked)
                }
            }
        }
        explorerStore.storeChecked.collect(scope) {
            val currentDir = explorerStore.current.value
            scope.launch {
                viewModel.updateTargets(currentDir, it)
            }
        }
        finderStore.notifications.collect(scope) {
            scope.launch {
                viewModel.onFinderTaskUpdate(it)
            }
        }
    }

    fun onDockGravityChange(gravity: Int) = preferenceStore.dockGravity.pushByEntity(gravity)

    fun onExplorerOptionSelected() = router.showExplorer()

    fun onConfigOptionSelected() = viewModel.switchConfigItemVisibility()

    fun onSettingsOptionSelected() = router.showSettings()

    fun onHistoryItemClick(node: String) = viewModel.replaceQuery.emitNow(node)
}