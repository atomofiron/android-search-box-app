package ru.atomofiron.regextool.iss.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.iss.service.ExplorerService
import ru.atomofiron.regextool.iss.service.model.XFile

class ExplorerInteractor {
    private val service = ExplorerService()

    fun getFiles(): List<XFile> = service.getFiles()

    fun openDir(dir: XFile, callback: (List<XFile>) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            service.openDir(dir) {
                GlobalScope.launch(Dispatchers.Main) {
                    callback(it)
                }
                service.persistState()
                service.updateDir(dir)
                service.cacheChildrenDirs(dir) {
                    GlobalScope.launch(Dispatchers.Main) {
                        callback(it)
                    }
                }
            }
        }
    }

    fun closeDir(dir: XFile, callback: (List<XFile>) -> Unit) {
        service.closeDir(dir, callback)
        service.persistState()
    }
}