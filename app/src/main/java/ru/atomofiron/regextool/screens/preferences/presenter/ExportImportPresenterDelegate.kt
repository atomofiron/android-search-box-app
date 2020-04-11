package ru.atomofiron.regextool.screens.preferences.presenter

import android.content.Context
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.injectable.channel.PreferenceChannel
import ru.atomofiron.regextool.injectable.service.PreferenceService
import ru.atomofiron.regextool.screens.preferences.PreferenceViewModel
import ru.atomofiron.regextool.screens.preferences.fragment.ExportImportFragmentDelegate
import ru.atomofiron.regextool.utils.Shell

class ExportImportPresenterDelegate(
        context: Context,
        private val viewModel: PreferenceViewModel,
        private val preferenceService: PreferenceService,
        private val preferenceChannel: PreferenceChannel
) : ExportImportFragmentDelegate.ExportImportOutput {
    override val externalPath: String = context.getExternalFilesDir(null)!!.absolutePath

    override fun exportPreferences() {
        val output = preferenceService.exportPreferences()
        showOutput(output, R.string.successful)
    }

    override fun exportHistory() {
        val output = preferenceService.exportHistory()
        showOutput(output, R.string.successful)
    }

    override fun importPreferences() {
        val output = preferenceService.importPreferences()
        showOutput(output, R.string.successful_with_restart)
    }

    override fun importHistory() {
        val output = preferenceService.importHistory()
        showOutput(output, R.string.successful)
        if (output.success) {
            preferenceChannel.historyImportedEvent.justNotify()
        }
    }

    private fun showOutput(output: Shell.Output, successMessage: Int) {
        when {
            output.success -> viewModel.alertOutputSuccess.invoke(successMessage)
            else -> viewModel.alertOutputError.invoke(output)
        }
    }
}