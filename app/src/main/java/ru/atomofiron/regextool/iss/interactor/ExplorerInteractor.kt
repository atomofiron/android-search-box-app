package ru.atomofiron.regextool.iss.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.iss.service.explorer.ExplorerService
import ru.atomofiron.regextool.iss.service.explorer.model.XFile

class ExplorerInteractor(private val service: ExplorerService) {
    val scope = CoroutineScope(Dispatchers.IO)

    fun setRoot(path: String) {
        scope.launch {
            service.setRoots(path)
        }
    }

    fun checkItem(item: XFile, isChecked: Boolean) {
        service.checkItem(item, isChecked)
    }

    fun openDir(dir: XFile) {
        scope.launch {
            service.openDir(dir)
            service.persistState()
        }
    }

    fun updateItem(file: XFile) {
        scope.launch {
            service.updateItem(file)
        }
    }

    fun invalidateItem(file: XFile) = service.invalidateDir(file)

    fun deleteItems(vararg file: XFile) {
        scope.launch {
            service.deleteItems(*file)
        }
    }
}