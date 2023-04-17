package app.atomofiron.searchboxapp.screens.finder

import android.Manifest
import android.os.Build
import android.os.Environment
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.Android
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

    init {
        viewState.run {
            uniqueItems.add(FinderStateItem.SearchAndReplaceItem())
            uniqueItems.add(FinderStateItem.SpecialCharactersItem(arrayOf()))
            uniqueItems.add(FinderStateItem.TestItem())
            uniqueItems.add(FinderStateItem.ButtonsItem)
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
        finderStore.tasksFlow.collect(scope) { tasks ->
            viewState.progressItems.clear()
            viewState.progressItems.addAll(tasks.map { FinderStateItem.ProgressItem(it) })
            viewState.updateState()
        }
    }

    fun onDockGravityChange(gravity: Int) = preferenceStore { setDockGravity(gravity) }

    fun onExplorerOptionSelected() {
        when {
            Build.VERSION.SDK_INT < Android.R -> {
                router.permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .granted { router.showExplorer() }
                    .denied { _, _ ->
                        viewState.showPermissionRequiredWarning()
                    }
            }
            Environment.isExternalStorageManager() -> router.showExplorer()
            else -> router.showSystemPermissionsAppSettings()
        }
    }

    fun onSettingsOptionSelected() = router.showSettings()

    fun onHistoryItemClick(node: String) = viewState.replaceQuery(node)

    fun onAllowStorageClick() = router.showSystemPermissionsAppSettings()
}