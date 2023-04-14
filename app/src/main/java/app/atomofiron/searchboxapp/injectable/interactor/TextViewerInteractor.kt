package app.atomofiron.searchboxapp.injectable.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.injectable.service.TextViewerService
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.SearchParams
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession

class TextViewerInteractor(
    private val scope: CoroutineScope,
    private val textViewerService: TextViewerService,
) {
    private val context = Dispatchers.IO

    fun fetchFileSession(path: String): TextViewerSession = textViewerService.getFileSession(path)

    /** invoke the callback after success */
    fun readFileToLine(item: Node, index: Int, callback: (() -> Unit)? = null) {
        scope.launch(context) {
            textViewerService.readFile(item, index) { success ->
                if (success) callback?.invoke()
            }
        }
    }

    fun search(item: Node, params: SearchParams) = textViewerService.search(item, params)

    fun removeTask(item: Node, taskId: Int) = textViewerService.removeTask(item, taskId)

    fun closeSession(item: Node) = textViewerService.closeSession(item)
}