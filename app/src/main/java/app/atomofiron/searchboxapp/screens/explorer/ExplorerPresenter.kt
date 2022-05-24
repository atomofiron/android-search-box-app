package app.atomofiron.searchboxapp.screens.explorer

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.value
import app.atomofiron.searchboxapp.custom.view.bottom_sheet_menu.BottomSheetMenuListener
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerItemActionListener
import app.atomofiron.searchboxapp.screens.explorer.places.PlacesAdapter
import app.atomofiron.searchboxapp.screens.explorer.places.XPlace
import app.atomofiron.searchboxapp.screens.explorer.places.XPlaceType
import app.atomofiron.searchboxapp.screens.explorer.presenter.BottomSheetMenuListenerDelegate
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerItemActionListenerDelegate
import app.atomofiron.searchboxapp.screens.explorer.presenter.PlacesActionListenerDelegate

class ExplorerPresenter(
    viewModel: ExplorerViewModel,
    router: ExplorerRouter,
    private val explorerStore: ExplorerStore,
    private val preferenceStore: PreferenceStore,
    appStore: AppStore,
    private val explorerInteractor: ExplorerInteractor,
    itemListener: ExplorerItemActionListenerDelegate,
    placesListener: PlacesActionListenerDelegate,
    menuListener: BottomSheetMenuListenerDelegate
) : BasePresenter<ExplorerViewModel, ExplorerRouter>(viewModel, router,),
    ExplorerItemActionListener by itemListener,
    PlacesAdapter.ItemActionListener by placesListener,
    BottomSheetMenuListener by menuListener {

    private val resources by appStore.resourcesProperty

    init {
        preferenceStore.dockGravity.collect(scope, ::onDockGravityChanged)
        preferenceStore.storagePath.collect(scope, ::onStoragePathChanged)
        preferenceStore.explorerItemComposition.collect(scope) {
            viewModel.itemComposition.value = it
        }

        val items = ArrayList<XPlace>()
        items.add(XPlace.InternalStorage(resources.getString(XPlaceType.InternalStorage.titleId), visible = true))
        items.add(XPlace.ExternalStorage(resources.getString(XPlaceType.ExternalStorage.titleId), visible = true))
        items.add(XPlace.AnotherPlace("Another Place 0"))
        items.add(XPlace.AnotherPlace("Another Place 1"))
        items.add(XPlace.AnotherPlace("Another Place 2"))
        viewModel.places.value = items

        onSubscribeData()
    }

    override fun onSubscribeData() {
        explorerStore.store.collect(scope, viewModel::onChanged)
        explorerStore.updates.collect(scope, viewModel::onChanged)
        explorerStore.current.collect(scope, viewModel::onChanged)
        explorerStore.alerts.collect(scope, viewModel.alerts::emit)
    }

    private fun onDockGravityChanged(gravity: Int) {
        viewModel.historyDrawerGravity.value = gravity
    }

    private fun onStoragePathChanged(path: String) = explorerInteractor.setRoot(path)

    fun onSearchOptionSelected() = router.showFinder()

    fun onOptionsOptionSelected() {
        val current = explorerStore.current.value
        val files = when {
            explorerStore.checked.isNotEmpty() -> ArrayList(explorerStore.checked)
            current != null -> listOf(current)
            else -> return
        }
        val ids = when {
            files.size > 1 -> viewModel.manyFilesOptions
            files.first().isChecked -> viewModel.manyFilesOptions
            files.first().isDirectory -> viewModel.directoryOptions
            else -> viewModel.oneFileOptions
        }
        viewModel.showOptions.value = ExplorerItemOptions(ids, files, viewModel.itemComposition.value)
    }

    fun onSettingsOptionSelected() = router.showSettings()

    fun onDockGravityChange(gravity: Int) = preferenceStore.dockGravity.pushByEntity(gravity)

    fun onCreateClick(dir: XFile, name: String, directory: Boolean) {
        explorerInteractor.create(dir, name, directory)
    }

    fun onRenameClick(item: XFile, name: String) = explorerInteractor.rename(item, name)

    fun onAllowStorageClick() = router.showSystemPermissionsAppSettings()

    fun onVolumeUp(isCurrentDirVisible: Boolean) = when {
        isCurrentDirVisible -> explorerInteractor.openParent()
        else -> viewModel.scrollToCurrentDir.invoke()
    }
}