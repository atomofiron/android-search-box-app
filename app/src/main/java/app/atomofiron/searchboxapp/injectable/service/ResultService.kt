package app.atomofiron.searchboxapp.injectable.service

import android.content.ClipData
import android.content.ClipboardManager
import androidx.work.WorkManager
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItem
import java.util.*

class ResultService(
    private val workManager: WorkManager,
    private val preferenceStore: PreferenceStore,
    private val clipboardManager: ClipboardManager,
) {

    fun stop(uuid: UUID) = workManager.cancelWorkById(uuid)

    fun copyToClipboard(item: Node) {
        val clip = ClipData.newPlainText(item.name, item.path)
        clipboardManager.setPrimaryClip(clip)
    }

    @Suppress("UNCHECKED_CAST")
    fun deleteItems(items: List<Node>, taskId: UUID) {
        /* todo go to ExplorerService
        items.forEach {
            it.setDeleting()
        }
        resultStore.itemsShellBeDeleted.invoke()
        val useSu = preferenceStore.useSu.value
        items.forEach {
            when (val error = Explorer.delete(useSu)) {
                null -> finderStore.deleteResultFromTask(it, uuid)
                else -> logI("deleteItems error != null $it\n$error")
            }
        }*/
    }

    fun cacheFile(item: ResultItem.Item) {
        val useSu = preferenceStore.useSu.value
        if (!item.item.isCached) {
            item.item.updateCache(useSu)
            //resultChannel.notifyItemChanged.value = item
        }
    }
}