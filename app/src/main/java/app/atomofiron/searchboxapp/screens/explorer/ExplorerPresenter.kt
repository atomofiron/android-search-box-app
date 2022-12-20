package app.atomofiron.searchboxapp.screens.explorer

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.custom.ExplorerView
import app.atomofiron.searchboxapp.injectable.channel.MainChannel
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerItemActionListener
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter
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
    private val curtainMenuDelegate: ExplorerCurtainMenuDelegate,
    mainChannel: MainChannel,
) : BasePresenter<ExplorerViewModel, ExplorerRouter>(scope, router),
    ExplorerView.ExplorerViewOutput,
    RootAdapter.RootClickListener,
    ExplorerItemActionListener by itemListener {

    private val currentTab get() = viewState.currentTab.value

    init {
        preferenceStore.explorerItemComposition.collect(scope) {
            viewState.itemComposition.value = it
        }
        mainChannel.maximized.collect(scope) {
            explorerInteractor.updateRoots(currentTab)
        }
    }

    override fun onSubscribeData() = Unit

    override fun onRootClick(item: NodeRoot) = explorerInteractor.selectRoot(currentTab, item)

    fun onSearchOptionSelected() = router.showFinder()

    fun onTabSelected(index: Int) {
        viewState.currentTab.value = when (index) {
            0 -> viewState.firstTab
            else -> viewState.secondTab
        }
        explorerInteractor.updateRoots(currentTab)
        explorerStore.current.value = viewState.getCurrentDir()
        /* todo searchTargets
            val checked = items.filter { it.isChecked }
            explorerStore.searchTargets.set(checked)
        */
    }

    fun onSettingsOptionSelected() = router.showSettings()

    fun onDockGravityChange(gravity: Int) = preferenceStore { setDockGravity(gravity) }

    fun onAllowStorageClick() = router.showSystemPermissionsAppSettings()

    fun onVolumeUp(isCurrentDirVisible: Boolean) {
        val currentDir = viewState.getCurrentDir()
        currentDir ?: return
        scrollOrOpenParent(currentDir, isCurrentDirVisible)
    }

    private fun scrollOrOpenParent(item: Node, isTargetVisible: Boolean) = when {
        isTargetVisible -> explorerInteractor.toggleDir(currentTab, item)
        else -> viewState.scrollTo(item)
    }
}