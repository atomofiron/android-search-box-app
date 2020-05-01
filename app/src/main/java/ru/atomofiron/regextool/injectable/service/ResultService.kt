package ru.atomofiron.regextool.injectable.service

import android.content.ClipData
import android.content.ClipboardManager
import androidx.work.WorkManager
import ru.atomofiron.regextool.model.finder.FinderResult
import java.util.*

class ResultService(
        private val workManager: WorkManager,
        private val clipboardManager: ClipboardManager
) {
    fun stop(uuid: UUID) = workManager.cancelWorkById(uuid)

    fun copyToClipboard(finderResult: FinderResult) {
        val clip = ClipData.newPlainText(finderResult.name, finderResult.completedPath)
        clipboardManager.setPrimaryClip(clip)
    }
}