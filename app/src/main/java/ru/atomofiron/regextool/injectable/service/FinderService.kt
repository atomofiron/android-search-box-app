package ru.atomofiron.regextool.injectable.service

import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import ru.atomofiron.regextool.injectable.channel.FinderStore
import ru.atomofiron.regextool.injectable.service.explorer.model.MutableXFile
import ru.atomofiron.regextool.injectable.service.explorer.model.XFile
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.model.finder.FinderTask
import ru.atomofiron.regextool.work.FinderWorker

class FinderService(
        private val workManager: WorkManager,
        private val explorerStore: ExplorerStore,
        private val finderStore: FinderStore,
        private val preferenceStore: PreferenceStore
) {
    fun search(query: String, where: List<XFile>, caseSensitive: Boolean, useRegex: Boolean, isMultiline: Boolean, forContent: Boolean) {
        val maxSize = preferenceStore.maxFileSizeForSearch.value
        val maxDepth = preferenceStore.maxDepthForSearch.value
        val useSu = preferenceStore.useSu.value
        val excludeDirs = preferenceStore.excludeDirs.value
        val textFormats = preferenceStore.textFormats.value

        val items = explorerStore.items
        val to = ArrayList<MutableXFile>()
        where.forEach { from ->
            for (i in 0 until items.size) {
                val item = items[i]
                if (from == item) {
                    to.add(item)
                    break
                }
            }
        }
        val inputData = FinderWorker.inputData(
                query, useSu, useRegex, maxSize, caseSensitive, excludeDirs, isMultiline, forContent,
                textFormats.split(' ').toTypedArray(), maxDepth, where.map { it.completedPath }.toTypedArray()
        )
        val request = OneTimeWorkRequest.Builder(FinderWorker::class.java)
                .setInputData(inputData)
                .build()
        workManager.beginWith(request).enqueue()
    }

    fun stop(task: FinderTask) = workManager.cancelWorkById(task.uuid)

    fun drop(task: FinderTask) = finderStore.drop(task)
}