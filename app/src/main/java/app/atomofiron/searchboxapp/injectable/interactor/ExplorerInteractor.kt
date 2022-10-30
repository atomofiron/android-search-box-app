package app.atomofiron.searchboxapp.injectable.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.injectable.service.ExplorerService
import app.atomofiron.searchboxapp.model.explorer.XFile

class ExplorerInteractor(
    private val scope: CoroutineScope,
    private val service: ExplorerService
) {
    private val context = Dispatchers.IO

    fun setRoot(path: String) {
        scope.launch(context) {
            service.trySetRoots(path)
        }
    }

    fun checkItem(item: XFile, isChecked: Boolean) {
        scope.launch(context) {
            service.tryCheckItem(item, isChecked)
        }
    }

    fun openDir(dir: XFile) {
        scope.launch(context) {
            service.tryOpen(dir)
            service.persistState()
        }
    }

    fun openParent() {
        scope.launch(context) {
            service.tryOpenParent()
        }
    }

    fun updateItem(file: XFile) {
        scope.launch(context) {
            service.tryUpdateItem(file)
        }
    }

    fun invalidateItem(file: XFile) = service.tryInvalidateDir(file)

    fun deleteItems(file: List<XFile>) {
        scope.launch(context) {
            service.tryDelete(file)
        }
    }

    fun rename(item: XFile, name: String) {
        scope.launch(context) {
            service.tryRename(item, name)
        }
    }

    fun create(dir: XFile, name: String, directory: Boolean) {
        scope.launch(context) {
            service.tryCreate(dir, name, directory)
        }
    }
}