package ru.atomofiron.regextool.injectable.service

import android.app.NotificationManager
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import ru.atomofiron.regextool.injectable.store.FinderStore
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.model.finder.FinderTask
import ru.atomofiron.regextool.work.FinderWorker
import java.util.*

class FinderService(
        private val workManager: WorkManager,
        private val notificationManager: NotificationManager,
        private val finderStore: FinderStore,
        private val preferenceStore: PreferenceStore
) {
    fun search(query: String, where: List<XFile>, ignoreCase: Boolean, useRegex: Boolean, isMultiline: Boolean, forContent: Boolean) {
        val maxSize = preferenceStore.maxFileSizeForSearch.value
        val maxDepth = preferenceStore.maxDepthForSearch.value
        val useSu = preferenceStore.useSu.value
        val excludeDirs = preferenceStore.excludeDirs.value
        val textFormats = preferenceStore.textFormats.value

        val inputData = FinderWorker.inputData(
                query, useSu, useRegex, maxSize, ignoreCase, excludeDirs, isMultiline, forContent,
                textFormats.split(' ').toTypedArray(), maxDepth, where.map { it.completedPath }.toTypedArray()
        )
        val request = OneTimeWorkRequest.Builder(FinderWorker::class.java)
                .setInputData(inputData)
                .build()
        workManager.beginWith(request).enqueue()
    }

    fun stop(uuid: UUID) = workManager.cancelWorkById(uuid)

    fun drop(task: FinderTask) {
        finderStore.drop(task)
        notificationManager.cancel(task.id.toInt())
    }
}