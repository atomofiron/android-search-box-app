package ru.atomofiron.regextool.iss.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.iss.service.ExplorerService
import ru.atomofiron.regextool.iss.service.model.Change
import ru.atomofiron.regextool.iss.service.model.XFile

class ExplorerInteractor {
    private val service = ExplorerService()

    val scope = CoroutineScope(Dispatchers.IO)

    fun setRoot(path: String) {
        scope.launch {
            service.addRoot(path)
        }
    }

    fun observeFiles(observer: (List<XFile>) -> Unit) {
        service.store.addObserver(observer)
    }

    fun observeUpdates(observer: (Change) -> Unit) {
        service.updates.addObserver(observer)
    }

    fun openDir(dir: XFile) {
        scope.launch {
            service.openDir(dir)
            service.persistState()
            service.updateFile(dir)
        }
    }

    fun updateFile(file: XFile) {
        scope.launch {
            service.updateFile(file)
        }
    }

    fun invalidateFile(file: XFile) = service.invalidateDir(file)
}