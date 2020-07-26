package app.atomofiron.searchboxapp.injectable.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.injectable.service.explorer.ExplorerService
import app.atomofiron.searchboxapp.model.explorer.XFile

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
            service.open(dir)
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

    fun deleteItems(file: List<XFile>) {
        scope.launch(context) {
            service.delete(file)
        }
    }

    fun rename(item: XFile, name: String) {
        scope.launch(context) {
            service.rename(item, name)
        }
    }

    fun create(dir: XFile, name: String, directory: Boolean) {
        scope.launch(context) {
            service.create(dir, name, directory)
        }
    }
}