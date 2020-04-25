package ru.atomofiron.regextool.screens.finder

import app.atomofiron.common.arch.BasePresenter
import ru.atomofiron.regextool.injectable.channel.PreferenceChannel
import ru.atomofiron.regextool.injectable.service.explorer.model.XFile
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.screens.finder.adapter.FinderAdapterOutput
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem
import ru.atomofiron.regextool.screens.finder.presenter.FinderAdapterPresenterDelegate

class FinderPresenter(
        viewModel: FinderViewModel,
        router: FinderRouter,
        private val finderAdapterDelegate: FinderAdapterPresenterDelegate,
        private val explorerStore: ExplorerStore,
        private val preferenceStore: PreferenceStore,
        private val preferenceChannel: PreferenceChannel
) : BasePresenter<FinderViewModel, FinderRouter>(viewModel, router),
        FinderAdapterOutput by finderAdapterDelegate
{
    private val items: ArrayList<FinderStateItem> get() = viewModel.items

    init {
        items.add(FinderStateItem.SearchAndReplaceItem())
        val characters = preferenceStore.specialCharacters.entity
        items.add(FinderStateItem.SpecialCharactersItem(characters))
        items.add(FinderStateItem.TestItem())
        items.add(FinderStateItem.ProgressItem(777, "9/36"))
        viewModel.state.value = items

        preferenceStore
                .dockGravity
                .addObserver(onClearedCallback) { gravity ->
                    viewModel.historyDrawerGravity.value = gravity
                }
        preferenceStore.specialCharacters.addObserver(onClearedCallback) { chs ->
            viewModel.updateItem(FinderStateItem.SpecialCharactersItem(chs))
        }
        preferenceChannel.historyImportedEvent.addObserver(onClearedCallback) {
            viewModel.reloadHistory.invoke()
        }

        explorerStore.current.addObserver(onClearedCallback) {
            val checked = explorerStore.storeChecked.value
            if (checked.isEmpty()) {
                updateTargets(it, checked)
            }
        }

        explorerStore.storeChecked.addObserver(onClearedCallback) {
            val currentDir = explorerStore.current.value
            updateTargets(currentDir, it)
        }
    }

    private fun updateTargets(currentDir: XFile?, checked: List<XFile>) {
        val targets = items.filterIsInstance<FinderStateItem.TargetItem>()
        targets.forEach { items.remove(it) }
        when {
            checked.isNotEmpty() -> checked.forEach { items.add(FinderStateItem.TargetItem(it)) }
            currentDir != null -> items.add(FinderStateItem.TargetItem(currentDir))
        }
        when {
            checked.isNotEmpty() -> finderAdapterDelegate.targets = checked
            currentDir != null -> finderAdapterDelegate.targets = arrayListOf(currentDir)
        }
        // todo replace all postValue() with coroutines
        viewModel.state.postValue(items)
    }

    fun onDockGravityChange(gravity: Int) = preferenceStore.dockGravity.push(gravity)

    fun onExplorerOptionSelected() = router.showExplorer()

    fun onConfigOptionSelected() = viewModel.switchConfigItemVisibility()

    fun onSettingsOptionSelected() = router.showSettings()

    fun onHistoryItemClick(node: String) = viewModel.replaceQuery.invoke(node)
}