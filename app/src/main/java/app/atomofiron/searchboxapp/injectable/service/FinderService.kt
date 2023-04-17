package app.atomofiron.searchboxapp.injectable.service

import android.app.NotificationManager
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.FinderStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.textviewer.SearchTask
import app.atomofiron.searchboxapp.work.FinderWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

class FinderService(
    private val scope: CoroutineScope,
    private val workManager: WorkManager,
    private val notificationManager: NotificationManager,
    private val finderStore: FinderStore,
    private val preferenceStore: PreferenceStore,
    explorerStore: ExplorerStore,
) {
    init {
        workManager.cancelAllWork()
        explorerStore.removed.collect(scope) {
            finderStore.deleteResultFromTasks(it)
        }
    }

    fun search(query: String, where: List<Node>, ignoreCase: Boolean, useRegex: Boolean, isMultiline: Boolean, forContent: Boolean) {
        val maxSize = preferenceStore.maxFileSizeForSearch.value
        val maxDepth = preferenceStore.maxDepthForSearch.value
        val useSu = preferenceStore.useSu.value
        val excludeDirs = preferenceStore.excludeDirs.value

        val inputData = FinderWorker.inputData(
            query, useSu, useRegex, maxSize, ignoreCase, excludeDirs, isMultiline, forContent,
            maxDepth, where.map { it.path }.toTypedArray(),
        )
        val request = OneTimeWorkRequest.Builder(FinderWorker::class.java)
            .setInputData(inputData)
            .build()
        workManager.beginWith(request).enqueue()
    }

    fun stop(uuid: UUID) = workManager.cancelWorkById(uuid)

    fun drop(task: SearchTask) {
        scope.launch {
            finderStore.drop(task)
        }
        notificationManager.cancel(task.uniqueId)
    }
}