package ru.atomofiron.regextool.injectable.interactor

import kotlinx.coroutines.*
import ru.atomofiron.regextool.injectable.service.explorer.ExplorerService
import ru.atomofiron.regextool.injectable.service.explorer.model.XFile

class ExplorerInteractor(
        private val scope: CoroutineScope,
        private val service: ExplorerService
) {
    private val context = Dispatchers.IO

    fun setRoot(path: String) {
        scope.launch(context) {
            service.setRoots(path)
        }
    }

    fun checkItem(item: XFile, isChecked: Boolean) {
        scope.launch(context) {
            service.checkItem(item, isChecked)
        }
    }

    fun openDir(dir: XFile) {
        scope.launch(context) {
            service.openDir(dir)
            service.persistState()
        }
    }

    fun openParent() {
        scope.launch(context) {
            service.openParent()
        }
    }

    fun updateItem(file: XFile) {
        scope.launch(context) {
            service.updateItem(file)
        }
    }

    fun invalidateItem(file: XFile) = service.invalidateDir(file)

    fun deleteItems(vararg file: XFile) {
        scope.launch(context) {
            service.deleteItems(*file)
        }
    }
}