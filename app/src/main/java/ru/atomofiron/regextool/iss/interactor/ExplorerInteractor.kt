package ru.atomofiron.regextool.iss.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.iss.service.ExplorerService
import ru.atomofiron.regextool.iss.service.model.XFile
import ru.atomofiron.regextool.log

class ExplorerInteractor {
    private val service = ExplorerService()

    private val scope = CoroutineScope(Dispatchers.IO)

    fun observeFiles(observer: (List<XFile>) -> Unit) {
        service.store.addObserver(observer)
    }

    fun observeUpdates(observer: (XFile?) -> Unit) {
        service.updates.addObserver(observer)
    }

    fun openDir(dir: XFile) {
        scope.launch {
            log("openDir")
            service.openDir(dir)
            service.persistState()
            log("updateFile")
            service.updateFile(dir)
        }
    }

    fun closeDir(dir: XFile) {
        log("closeDir")
        scope.launch {
            service.closeDir(dir)
            service.persistState()
        }
    }

    fun updateFile(file: XFile) {
        log("updateFile")
        scope.launch {
            service.updateFile(file)
        }
    }

    fun invalidateFile(file: XFile) {
        log("invalidateFile")
        scope.launch {
            service.invalidateDir(file)
        }
    }
}