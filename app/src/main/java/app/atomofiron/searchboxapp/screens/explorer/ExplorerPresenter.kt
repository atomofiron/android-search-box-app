package app.atomofiron.searchboxapp.screens.explorer

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.bottom_sheet_menu.BottomSheetMenuListener
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerItemActionListener
import app.atomofiron.searchboxapp.screens.explorer.places.PlacesAdapter
import app.atomofiron.searchboxapp.screens.explorer.places.XPlace
import app.atomofiron.searchboxapp.screens.explorer.presenter.BottomSheetMenuListenerDelegate
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerItemActionListenerDelegate
import app.atomofiron.searchboxapp.screens.explorer.presenter.PlacesActionListenerDelegate

class ExplorerPresenter(
        viewModel: ExplorerViewModel,
        router: ExplorerRouter,
        private val explorerStore: ExplorerStore,
        private val preferenceStore: PreferenceStore,
        private val explorerInteractor: ExplorerInteractor,
        itemListener: ExplorerItemActionListenerDelegate,
        placesListener: PlacesActionListenerDelegate,
        menuListener: BottomSheetMenuListenerDelegate
) : BasePresenter<ExplorerViewModel, ExplorerRouter>(
        viewModel,
        router,
        coroutineScope = null,
        permissionResultListener = itemListener.permissions
),
        ExplorerItemActionListener by itemListener,
        PlacesAdapter.ItemActionListener by placesListener,
        BottomSheetMenuListener by menuListener {

    init {
        preferenceStore.dockGravity.addObserver(onClearedCallback, ::onDockGravityChanged)
        preferenceStore.storagePath.addObserver(onClearedCallback, ::onStoragePathChanged)
        preferenceStore.explorerItemComposition.addObserver(onClearedCallback, viewModel.itemComposition::setValue)

        val items = ArrayList<XPlace>()
        items.add(XPlace.InternalStorage(context.getString(R.string.internal_storage), visible = true))
        items.add(XPlace.ExternalStorage(context.getString(R.string.external_storage), visible = true))
        items.add(XPlace.AnotherPlace("Another Place 0"))
        items.add(XPlace.AnotherPlace("Another Place 1"))
        items.add(XPlace.AnotherPlace("Another Place 2"))
        viewModel.places.value = items

        onSubscribeData()
    }

    override fun onSubscribeData() {
        explorerStore.store.addObserver(onClearedCallback, viewModel::onChanged)
        explorerStore.updates.addObserver(onClearedCallback, viewModel::onChanged)
        explorerStore.current.addObserver(onClearedCallback, viewModel::onChanged)
        explorerStore.alerts.addObserver(onClearedCallback, viewModel.alerts::invoke)
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
            files[0].isChecked -> viewModel.manyFilesOptions
            files[0].isDirectory -> viewModel.directoryOptions
            else -> viewModel.oneFileOptions
        }
        viewModel.showOptions.invoke(ExplorerItemOptions(ids, files, viewModel.itemComposition.value))
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