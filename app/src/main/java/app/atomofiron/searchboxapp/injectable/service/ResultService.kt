package app.atomofiron.searchboxapp.injectable.service

import android.content.ClipData
import android.content.ClipboardManager
import androidx.work.WorkManager
import app.atomofiron.searchboxapp.injectable.channel.ResultChannel
import app.atomofiron.searchboxapp.injectable.store.FinderStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.injectable.store.ResultStore
import app.atomofiron.searchboxapp.logI
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.finder.FinderResult
import app.atomofiron.searchboxapp.screens.result.adapter.FinderResultItem
import java.util.*

class ResultService(
        private val workManager: WorkManager,
        private val resultChannel: ResultChannel,
        private val resultStore: ResultStore,
        private val finderStore: FinderStore,
        private val preferenceStore: PreferenceStore,
        private val clipboardManager: ClipboardManager
) {
    fun stop(uuid: UUID) = workManager.cancelWorkById(uuid)

    fun dropTaskError(taskId: Long) = finderStore.dropTaskError(taskId)

    fun copyToClipboard(finderResult: FinderResult) {
        val clip = ClipData.newPlainText(finderResult.name, finderResult.completedPath)
        clipboardManager.setPrimaryClip(clip)
    }

    @Suppress("UNCHECKED_CAST")
    fun deleteItems(items: List<XFile>, uuid: UUID) {
        items as List<FinderResult>
        items.forEach {
            it.willBeDeleted()
        }
        resultStore.itemsShellBeDeleted.justNotify()
        val useSu = preferenceStore.useSu.value
        items.forEach {
            when (val error = it.delete(useSu)) {
                null -> finderStore.deleteResultFromTask(it, uuid)
                else -> logI("deleteItems error != null $it\n$error")
            }
        }
    }

    fun cacheFile(item: FinderResultItem.Item) {
        val useSu = preferenceStore.useSu.value
        if (!item.item.isCached) {
            item.item.updateCache(useSu)
            resultChannel.notifyItemChanged.setAndNotify(item)
        }
    }
}