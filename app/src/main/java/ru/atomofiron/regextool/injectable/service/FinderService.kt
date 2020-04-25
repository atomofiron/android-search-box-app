package ru.atomofiron.regextool.injectable.service

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import ru.atomofiron.regextool.injectable.service.explorer.model.MutableXFile
import ru.atomofiron.regextool.injectable.service.explorer.model.XFile
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.work.FinderWorker

class FinderService(
        private val context: Context,
        private val explorerStore: ExplorerStore,
        private val preferenceStore: PreferenceStore
) {
    fun search(query: String, where: List<XFile>, caseSensitive: Boolean, useRegex: Boolean, isMultiline: Boolean, forContent: Boolean) {
        val maxSize = preferenceStore.maxFileSizeForSearch.value
        val maxDepth = preferenceStore.maxDepthForSearch.value
        val useSu = preferenceStore.useSu.value
        val excludeDirs = preferenceStore.excludeDirs.value
        val textFormats = preferenceStore.textFormats.value

        val manager = WorkManager.getInstance(context)

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
        manager.beginWith(request).enqueue()
    }
}