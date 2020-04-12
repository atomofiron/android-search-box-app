package ru.atomofiron.regextool.screens.finder

import app.atomofiron.common.arch.BasePresenter
import ru.atomofiron.regextool.injectable.channel.PreferenceChannel
import ru.atomofiron.regextool.injectable.service.explorer.model.XFile
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.SettingsStore
import ru.atomofiron.regextool.screens.finder.adapter.FinderAdapterOutput
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem
import ru.atomofiron.regextool.screens.finder.model.FinderStateItemUpdate
import ru.atomofiron.regextool.screens.finder.presenter.FinderAdapterPresenterDelegate

class FinderPresenter(
        viewModel: FinderViewModel,
        router: FinderRouter,
        finderAdapterDelegate: FinderAdapterPresenterDelegate,
        private val explorerStore: ExplorerStore,
        private val settingsStore: SettingsStore,
        private val preferenceChannel: PreferenceChannel
) : BasePresenter<FinderViewModel, FinderRouter>(viewModel, router),
        FinderAdapterOutput by finderAdapterDelegate
{
    private val items: ArrayList<FinderStateItem> get() = viewModel.items
    private var configItem: FinderStateItem.ConfigItem? = FinderStateItem.ConfigItem()

    init {
        items.add(FinderStateItem.SearchAndReplaceItem())
        val characters = settingsStore.specialCharacters.entity
        items.add(FinderStateItem.SpecialCharactersItem(characters))
        items.add(FinderStateItem.TestItem())
        items.add(FinderStateItem.ProgressItem(777, "9/36"))
        viewModel.state.value = items

        settingsStore
                .dockGravity
                .addObserver(onClearedCallback) { gravity ->
                    viewModel.historyDrawerGravity.value = gravity
                }
        settingsStore.specialCharacters.addObserver(onClearedCallback) { chs ->
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
        // todo replace all postValue() with coroutines
        viewModel.state.postValue(items)
    }

    fun onDockGravityChange(gravity: Int) = settingsStore.dockGravity.push(gravity)

    fun onExplorerOptionSelected() = router.showExplorer()

    fun onConfigOptionSelected() {
        when (val configItem = configItem) {
            null -> {
                val item = viewModel.getItem(FinderStateItem.ConfigItem::class)
                this.configItem = item
                val index = items.indexOf(item)
                items.removeAt(index)
                viewModel.updateContent.invoke(FinderStateItemUpdate.Removed(index))
            }
            else -> {
                val index = items.indexOf(viewModel.getItem(FinderStateItem.TestItem::class))
                items.add(index, configItem)
                viewModel.updateContent.invoke(FinderStateItemUpdate.Inserted(index, configItem))
                this.configItem = null
            }
        }
    }

    fun onSettingsOptionSelected() = router.showSettings()

    fun onHistoryItemClick(node: String) = viewModel.replaceQuery.invoke(node)
}