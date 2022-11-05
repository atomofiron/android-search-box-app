package app.atomofiron.searchboxapp.injectable.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.injectable.service.ResultService
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.FinderResult
import app.atomofiron.searchboxapp.screens.result.adapter.FinderResultItem
import java.util.*

class ResultInteractor(
    private val scope: CoroutineScope,
    private val resultService: ResultService
) {
    private val context = Dispatchers.IO

    fun stop(uuid: UUID) = resultService.stop(uuid)

    fun dropTaskError(taskId: Long) = resultService.dropTaskError(taskId)

    fun copyToClipboard(finderResult: FinderResult) = resultService.copyToClipboard(finderResult)

    fun deleteItems(items: List<Node>, uuid: UUID) {
        scope.launch(context) {
            resultService.deleteItems(items, uuid)
        }
    }

    fun cacheFile(item: FinderResultItem.Item) {
        scope.launch(context) {
            resultService.cacheFile(item)
        }
    }
}