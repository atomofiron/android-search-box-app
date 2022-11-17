package app.atomofiron.searchboxapp.screens.explorer

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerItemActionListener
import app.atomofiron.searchboxapp.screens.explorer.places.PlacesAdapter
import app.atomofiron.searchboxapp.screens.explorer.places.XPlace
import app.atomofiron.searchboxapp.screens.explorer.places.XPlaceType
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerCurtainMenuDelegate
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerItemActionListenerDelegate
import app.atomofiron.searchboxapp.screens.explorer.presenter.PlacesActionListenerDelegate
import kotlinx.coroutines.CoroutineScope

class ExplorerPresenter(
    scope: CoroutineScope,
    private val viewState: ExplorerViewState,
    router: ExplorerRouter,
    private val explorerStore: ExplorerStore,
    private val preferenceStore: PreferenceStore,
    appStore: AppStore,
    private val explorerInteractor: ExplorerInteractor,
    itemListener: ExplorerItemActionListenerDelegate,
    placesListener: PlacesActionListenerDelegate,
    private val curtainMenuDelegate: ExplorerCurtainMenuDelegate
) : BasePresenter<ExplorerViewModel, ExplorerRouter>(scope, router),
    ExplorerItemActionListener by itemListener,
    PlacesAdapter.ItemActionListener by placesListener {

    private val resources by appStore.resourcesProperty

    init {
        preferenceStore.dockGravity.collect(scope, ::onDockGravityChanged)
        preferenceStore.storagePath.collect(scope) { path ->
            explorerInteractor.setRoot(path)
        }
        preferenceStore.explorerItemComposition.collect(scope) {
            viewState.itemComposition.value = it
        }

        val items = ArrayList<XPlace>()
        items.add(XPlace.InternalStorage(resources.getString(XPlaceType.InternalStorage.titleId), visible = true))
        items.add(XPlace.ExternalStorage(resources.getString(XPlaceType.ExternalStorage.titleId), visible = true))
        items.add(XPlace.AnotherPlace("Another Place 0"))
        items.add(XPlace.AnotherPlace("Another Place 1"))
        items.add(XPlace.AnotherPlace("Another Place 2"))
        viewState.places.value = items
    }

    override fun onSubscribeData() = Unit

    private fun onDockGravityChanged(gravity: Int) {
        viewState.historyDrawerGravity.value = gravity
    }

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

    fun onVolumeUp(isCurrentDirVisible: Boolean) = when {
        isCurrentDirVisible -> explorerInteractor.openParent()
        else -> viewState.scrollToCurrentDir()
    }
}