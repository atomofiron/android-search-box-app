package app.atomofiron.searchboxapp.injectable.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.injectable.service.TextViewerService
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.FinderQueryParams
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession

class TextViewerInteractor(
    private val scope: CoroutineScope,
    private val textViewerService: TextViewerService,
) {
    private val context = Dispatchers.IO

    fun fetchFileSession(path: String): TextViewerSession = textViewerService.getFileSession(path)

    fun fetchTask(item: Node, taskId: Long) = textViewerService.fetchTask(item, taskId)

    /** invoke the callback after success */
    fun readFileToLine(item: Node, index: Int, callback: (() -> Unit)? = null) {
        scope.launch(context) {
            val success = textViewerService.readFile(item, index)
            if (success) callback?.invoke()
        }
    }

    fun search(item: Node, params: FinderQueryParams) = textViewerService.search(item, params)

    fun removeTask(item: Node, taskId: Long) = textViewerService.removeTask(item, taskId)

    fun closeSession(item: Node) = textViewerService.closeSession(item)
}