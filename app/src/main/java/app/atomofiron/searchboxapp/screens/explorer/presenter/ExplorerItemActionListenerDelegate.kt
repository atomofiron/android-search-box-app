package app.atomofiron.searchboxapp.screens.explorer.presenter

import android.Manifest
import androidx.fragment.app.Fragment
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.value
import app.atomofiron.common.util.permission.PermissionResultListener
import app.atomofiron.common.util.permission.Permissions
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.explorer.ExplorerRouter
import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewModel
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerItemActionListener

class ExplorerItemActionListenerDelegate(
    fragment: WeakProperty<Fragment>,
    private val viewModel: ExplorerViewModel,
    private val explorerStore: ExplorerStore,
    private val preferenceStore: PreferenceStore,
    private val router: ExplorerRouter,
    private val explorerInteractor: ExplorerInteractor
) : ExplorerItemActionListener, PermissionResultListener {
    private var readStorageGranted = false
    val permissions = Permissions(fragment)

    override fun onItemLongClick(item: XFile) {
        val files: List<XFile> = when {
            item.isChecked -> explorerStore.checked
            else -> listOf(item)
        }
        val ids = when {
            files.size > 1 -> viewModel.manyFilesOptions
            files[0].isRoot -> viewModel.rootOptions
            files[0].isChecked -> viewModel.manyFilesOptions
            files[0].isDirectory -> viewModel.directoryOptions
            else -> viewModel.oneFileOptions
        }
        val options = ExplorerItemOptions(ids, files, viewModel.itemComposition.value)
        viewModel.showOptions.value = options
    }

    override fun onItemClick(item: XFile) {
        val useSu = preferenceStore.useSu.value
        when {
            !useSu && !readStorageGranted -> permissions
                    .check(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .granted {
                        readStorageGranted = true
                        onItemClick(item)
                    }
                    .forbidden { viewModel.permissionRequiredWarning.invoke() }
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