package app.atomofiron.searchboxapp.screens.explorer.presenter

import android.Manifest
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import app.atomofiron.common.util.Android
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.explorer.ExplorerRouter
import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewModel
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerItemActionListener

class ExplorerItemActionListenerDelegate(
    private val viewModel: ExplorerViewModel,
    private val menuListenerDelegate: ExplorerCurtainMenuDelegate,
    private val explorerStore: ExplorerStore,
    private val preferenceStore: PreferenceStore,
    private val router: ExplorerRouter,
    private val explorerInteractor: ExplorerInteractor,
) : ExplorerItemActionListener {

    override fun onItemLongClick(item: Node) {
        val files: List<Node> = when {
            item.isChecked -> explorerStore.checked.value
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
        menuListenerDelegate.showOptions(options)
    }

    override fun onItemClick(item: Node) {
        val useSu = preferenceStore.useSu.value
        when {
            useSu -> openItem(item)
            SDK_INT < Android.R -> {
                router.permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .granted {
                        openItem(item)
                    }
                    .denied { _, _ ->
                        viewModel.showPermissionRequiredWarning()
                    }
            }
            Environment.isExternalStorageManager() -> openItem(item)
            else -> router.showSystemPermissionsAppSettings()
        }
    }

    private fun openItem(item: Node) {
        when {
            item.isDirectory -> explorerInteractor.toggleDir(item)
            else -> {
                val textFormats = preferenceStore.textFormats.entity
                router.showFile(item, textFormats)
            }
        }
    }

    override fun onItemCheck(item: Node, isChecked: Boolean) = explorerInteractor.checkItem(item, isChecked)

    override fun onItemVisible(item: Node) = explorerInteractor.updateItem(item)
}