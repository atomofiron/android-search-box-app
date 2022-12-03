package app.atomofiron.searchboxapp.screens.explorer

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerItemActionListener
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerCurtainMenuDelegate
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerItemActionListenerDelegate
import kotlinx.coroutines.CoroutineScope

class ExplorerPresenter(
    scope: CoroutineScope,
    private val viewState: ExplorerViewState,
    router: ExplorerRouter,
    private val explorerStore: ExplorerStore,
    private val preferenceStore: PreferenceStore,
    private val explorerInteractor: ExplorerInteractor,
    itemListener: ExplorerItemActionListenerDelegate,
    private val curtainMenuDelegate: ExplorerCurtainMenuDelegate
) : BasePresenter<ExplorerViewModel, ExplorerRouter>(scope, router),
    ExplorerItemActionListener by itemListener {

    init {
        preferenceStore.explorerItemComposition.collect(scope) {
            viewState.itemComposition.value = it
        }

    }

    override fun onSubscribeData() = Unit

    fun onSearchOptionSelected() = router.showFinder()

    fun onOptionsOptionSelected() {
        val current = explorerStore.current.value
        val checked = explorerStore.checked.value
        val files = when {
            checked.isNotEmpty() -> ArrayList(checked)
            current != null -> listOf(current)
            else -> return
        }
        val ids = when {
            files.size > 1 -> viewState.manyFilesOptions
            files.first().isRoot -> viewState.rootOptions
            files.first().isDirectory -> viewState.directoryOptions
            else -> viewState.oneFileOptions
        }
        val options = ExplorerItemOptions(ids, files, viewState.itemComposition.value)
        curtainMenuDelegate.showOptions(options)
    }

    fun onSettingsOptionSelected() = router.showSettings()

    fun onDockGravityChange(gravity: Int) = preferenceStore { setDockGravity(gravity) }

    fun onAllowStorageClick() = router.showSystemPermissionsAppSettings()

    fun onSeparatorClick(item: Node, isTargetVisible: Boolean) = scrollOrOpenParent(item, isTargetVisible)

    fun onVolumeUp(isCurrentDirVisible: Boolean) {
        val currentDir = viewState.current.value
        currentDir ?: return
        scrollOrOpenParent(currentDir, isCurrentDirVisible)
    }

    private fun scrollOrOpenParent(item: Node, isTargetVisible: Boolean) = when {
        isTargetVisible -> explorerInteractor.toggleDir(item)
        else -> viewState.scrollTo(item)
    }
}