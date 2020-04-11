package ru.atomofiron.regextool.screens.explorer.presenter

import app.atomofiron.common.arch.BasePresenter
import kotlinx.coroutines.CoroutineScope
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.injectable.interactor.ExplorerInteractor
import ru.atomofiron.regextool.injectable.service.explorer.model.XFile
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.SettingsStore
import ru.atomofiron.regextool.screens.explorer.ExplorerRouter
import ru.atomofiron.regextool.screens.explorer.ExplorerViewModel
import ru.atomofiron.regextool.screens.explorer.adapter.ExplorerItemActionListener
import ru.atomofiron.regextool.screens.explorer.places.PlacesAdapter
import ru.atomofiron.regextool.screens.explorer.places.XPlace
import ru.atomofiron.regextool.screens.explorer.sheet.BottomSheetMenuWithTitle
import ru.atomofiron.regextool.view.custom.bottom_sheet_menu.BottomSheetMenuListener

class ExplorerPresenter(
        viewModel: ExplorerViewModel,
        scope: CoroutineScope,
        override val router: ExplorerRouter,
        private val explorerStore: ExplorerStore,
        private val settingsStore: SettingsStore,
        private val explorerInteractor: ExplorerInteractor,
        itemListener: ExplorerItemActionListenerDelegate,
        placesListener: PlacesActionListenerDelegate,
        menuListener: BottomSheetMenuListenerDelegate
) : BasePresenter<ExplorerViewModel, ExplorerRouter>(viewModel, scope, itemListener.permissions),
        ExplorerItemActionListener by itemListener,
        PlacesAdapter.ItemActionListener by placesListener,
        BottomSheetMenuListener by menuListener {

    init {
        settingsStore.dockGravity.addObserver(onClearedCallback, ::onDockGravityChanged)
        settingsStore.storagePath.addObserver(onClearedCallback, ::onStoragePathChanged)
        settingsStore.explorerItemComposition.addObserver(onClearedCallback, viewModel.itemComposition::setValue)

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
            current != null -> arrayListOf(current)
            else -> return
        }
        val ids = when {
            files.size > 1 -> viewModel.manyFilesOptions
            files[0].isChecked -> viewModel.manyFilesOptions
            files[0].isDirectory -> viewModel.directoryOptions
            else -> viewModel.oneFileOptions
        }
        viewModel.showOptions.invoke(BottomSheetMenuWithTitle.ExplorerItemOptions(ids, files, viewModel.itemComposition.value))
    }

    fun onSettingsOptionSelected() = router.showSettings()

    fun onDockGravityChange(gravity: Int) = settingsStore.dockGravity.push(gravity)

    fun onCreateClick(dir: XFile, name: String, directory: Boolean) {
        // todo next
    }

    fun onRenameClick(item: XFile, name: String) {
        // todo next
    }

    fun onAllowStorageClick() = router.showSystemPermissionsAppSettings()

    fun onVolumeUp() {
        explorerInteractor.openParent()
        viewModel.scrollToCurrentDir.invoke()
    }
}