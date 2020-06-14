package ru.atomofiron.regextool.screens.finder

import app.atomofiron.common.arch.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.injectable.channel.PreferenceChannel
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.FinderStore
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.screens.finder.adapter.FinderAdapterOutput
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem
import ru.atomofiron.regextool.screens.finder.presenter.FinderAdapterPresenterDelegate

class FinderPresenter(
        viewModel: FinderViewModel,
        router: FinderRouter,
        private val scope: CoroutineScope,
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
        preferenceStore.dockGravity.addObserver(onClearedCallback) { gravity ->
            viewModel.historyDrawerGravity.value = gravity
        }
        preferenceStore.specialCharacters.addObserver(onClearedCallback) { chs ->
            viewModel.updateUniqueItem(FinderStateItem.SpecialCharactersItem(chs))
        }
        preferenceChannel.historyImportedEvent.addObserver(onClearedCallback, viewModel.reloadHistory::invoke)

        explorerStore.current.addObserver(onClearedCallback) {
            val checked = explorerStore.storeChecked.value
            if (checked.isEmpty()) {
                scope.launch {
                    viewModel.updateTargets(it, checked)
                }
            }
        }
        explorerStore.storeChecked.addObserver(onClearedCallback) {
            val currentDir = explorerStore.current.value
            scope.launch {
                viewModel.updateTargets(currentDir, it)
            }
        }
        finderStore.notifications.addObserver(onClearedCallback) {
            scope.launch {
                viewModel.onFinderTaskUpdate(it)
            }
        }
    }

    fun onDockGravityChange(gravity: Int) = preferenceStore.dockGravity.pushByEntity(gravity)

    fun onExplorerOptionSelected() = router.showExplorer()

    fun onConfigOptionSelected() = viewModel.switchConfigItemVisibility()

    fun onSettingsOptionSelected() = router.showSettings()

    fun onHistoryItemClick(node: String) = viewModel.replaceQuery.invoke(node)
}