package ru.atomofiron.regextool.iss.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.iss.service.ExplorerService
import ru.atomofiron.regextool.iss.service.model.XFile
import ru.atomofiron.regextool.tik

class ExplorerInteractor {
    private val service = ExplorerService()

    fun getFiles(): List<XFile> = service.getFiles()

    fun openDir(dir: XFile, callback: (List<XFile>) -> Unit) {
        tik("openDir")
        GlobalScope.launch(Dispatchers.IO) {
            service.openDir(dir) {
                tik("openDir callback")
                GlobalScope.launch(Dispatchers.Main) {
                tik("interactor callback")
                    callback(it)
                }
            }
            service.persistState()
            tik("cacheChildrenDirs")
            service.cacheChildrenDirs(dir)
            tik("cacheChildrenDirs end")
        }
        tik("openDir end")
    }

    fun closeDir(dir: XFile, callback: (List<XFile>) -> Unit) {
        service.closeDir(dir, callback)
        service.persistState()
    }
}