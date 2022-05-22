package app.atomofiron.searchboxapp.screens.preferences.presenter

import android.content.Context
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.value
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.service.PreferenceService
import app.atomofiron.searchboxapp.model.explorer.MutableXFile
import app.atomofiron.searchboxapp.screens.preferences.PreferenceViewModel
import app.atomofiron.searchboxapp.screens.preferences.fragment.ExportImportFragmentDelegate
import app.atomofiron.searchboxapp.utils.Shell

class ExportImportPresenterDelegate(
    context: Context,
    private val viewModel: PreferenceViewModel,
    private val preferenceService: PreferenceService,
    private val preferenceChannel: PreferenceChannel
) : ExportImportFragmentDelegate.ExportImportOutput {
    override val externalPath = MutableXFile.completePathAsDir(context.getExternalFilesDir(null)!!.absolutePath)

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
            preferenceChannel.historyImportedEvent.invoke()
        }
    }

    private fun showOutput(output: Shell.Output, successMessage: Int) {
        when {
            output.success -> viewModel.alertOutputSuccess.value = successMessage
            else -> viewModel.alertOutputError.value = output
        }
    }
}