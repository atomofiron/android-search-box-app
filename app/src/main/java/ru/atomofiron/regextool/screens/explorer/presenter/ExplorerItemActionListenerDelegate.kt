package ru.atomofiron.regextool.screens.explorer.presenter

import android.Manifest
import app.atomofiron.common.util.permission.PermissionResultListener
import app.atomofiron.common.util.permission.Permissions
import app.atomofiron.common.util.property.WeakProperty
import ru.atomofiron.regextool.injectable.interactor.ExplorerInteractor
import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.model.other.ExplorerItemOptions
import ru.atomofiron.regextool.screens.explorer.ExplorerFragment
import ru.atomofiron.regextool.screens.explorer.ExplorerRouter
import ru.atomofiron.regextool.screens.explorer.ExplorerViewModel
import ru.atomofiron.regextool.screens.explorer.adapter.ExplorerItemActionListener

class ExplorerItemActionListenerDelegate(
        fragment: WeakProperty<ExplorerFragment>,
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
            else -> arrayListOf(item)
        }
        val ids = when {
            files.size > 1 -> viewModel.manyFilesOptions
            files[0].isRoot -> viewModel.rootOptions
            files[0].isChecked -> viewModel.manyFilesOptions
            files[0].isDirectory -> viewModel.directoryOptions
            else -> viewModel.oneFileOptions
        }
        val options = ExplorerItemOptions(ids, files, viewModel.itemComposition.value)
        viewModel.showOptions.invoke(options)
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