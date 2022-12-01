package app.atomofiron.searchboxapp.injectable.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.injectable.service.ExplorerService
import app.atomofiron.searchboxapp.model.explorer.Node

class ExplorerInteractor(
    private val scope: CoroutineScope,
    private val service: ExplorerService
) {
    private val context = Dispatchers.IO

    fun setRoot(path: String) {
        scope.launch(context) {
            service.trySetRoots(listOf(path))
        }
    }

    fun checkItem(item: Node, isChecked: Boolean) {
        scope.launch(context) {
            service.tryCheckItem(item, isChecked)
        }
    }

    fun toggleDir(dir: Node) {
        scope.launch(context) {
            service.tryToggle(dir)
        }
    }

    fun updateItem(file: Node) {
        scope.launch(context) {
            service.tryCacheAsync(file)
        }
    }

    fun deleteItems(items: List<Node>) {
        scope.launch(context) {
            service.tryDelete(items)
        }
    }

    fun rename(item: Node, name: String) {
        scope.launch(context) {
            service.tryRename(item, name)
        }
    }

    fun create(dir: Node, name: String, directory: Boolean) {
        scope.launch(context) {
            service.tryCreate(dir, name, directory)
        }
    }
}