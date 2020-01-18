package ru.atomofiron.regextool.iss.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.iss.service.ExplorerService
import ru.atomofiron.regextool.iss.service.model.XFile

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
            service.openDir(dir)
            service.persistState()
            service.updateDir(dir)
        }
    }

    fun closeDir(dir: XFile) {
        scope.launch {
            service.closeDir(dir)
            service.persistState()
        }
    }

    fun cacheDir(dir: XFile) {
        scope.launch {
            service.cacheDir(dir)
        }
    }
}