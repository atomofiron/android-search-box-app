package app.atomofiron.searchboxapp.screens.explorer.presenter

import android.Manifest
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.value
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.explorer.ExplorerRouter
import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewModel
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerItemActionListener

class ExplorerItemActionListenerDelegate(
    private val viewModel: ExplorerViewModel,
    private val explorerStore: ExplorerStore,
    private val preferenceStore: PreferenceStore,
    private val router: ExplorerRouter,
    private val explorerInteractor: ExplorerInteractor
) : ExplorerItemActionListener {

    override fun onItemLongClick(item: XFile) {
        val files: List<XFile> = when {
            item.isChecked -> explorerStore.checked
            else -> listOf(item)
        }
        val ids = when {
            files.size > 1 -> viewModel.manyFilesOptions
            files.first().isRoot -> viewModel.rootOptions
            files.first().isChecked -> viewModel.manyFilesOptions
            files.first().isDirectory -> viewModel.directoryOptions
            else -> viewModel.oneFileOptions
        }
        val options = ExplorerItemOptions(ids, files, viewModel.itemComposition.value)
        viewModel.showOptions.value = options
    }

    override fun onItemClick(item: XFile) {
        val useSu = preferenceStore.useSu.value
        when {
            useSu -> openItem(item)
            SDK_INT < Android.R -> {
                val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                router.permissions.request(permission)
                    .granted {
                        openItem(item)
                    }
                    .denied { _, _ ->
                        viewModel.permissionRequiredWarning.invoke()
                    }
            }
            Environment.isExternalStorageManager() -> openItem(item)
            else -> router.showSystemPermissionsAppSettings()
        }
    }

    private fun openItem(item: XFile) {
        when {
            item.isDirectory -> explorerInteractor.openDir(item)
            else -> {
                val textFormats = preferenceStore.textFormats.entity
                router.showFile(item, textFormats)
            }
        }
    }

    override fun onItemCheck(item: XFile, isChecked: Boolean) = explorerInteractor.checkItem(item, isChecked)

    override fun onItemVisible(item: XFile) = explorerInteractor.updateItem(item)

    override fun onItemInvalidate(item: XFile) = explorerInteractor.invalidateItem(item)
}