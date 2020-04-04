package ru.atomofiron.regextool.screens.preferences

import android.app.Application
import app.atomofiron.common.base.BaseViewModel
import app.atomofiron.common.util.SingleLiveEvent
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.channel.PreferencesChannel
import ru.atomofiron.regextool.di.DaggerInjector
import ru.atomofiron.regextool.iss.service.PreferenceService
import ru.atomofiron.regextool.iss.store.SettingsStore
import ru.atomofiron.regextool.model.ExplorerItemComposition
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Shell
import javax.inject.Inject

class PreferenceViewModel(app: Application) : BaseViewModel<PreferenceRouter>(app) {
    override val router = PreferenceRouter()

    val alert = SingleLiveEvent<String>()
    val alertOutputSuccess = SingleLiveEvent<Int>()
    val alertOutputError = SingleLiveEvent<Shell.Output>()
    val externalPath: String get() = app.applicationContext.getExternalFilesDir(null)!!.absolutePath
    val isExportImportAvailable: Boolean get() = app.applicationContext.getExternalFilesDir(null) != null
    val explorerItemState: ExplorerItemComposition get() = settingsStore.explorerItem.entity

    @Inject
    lateinit var preferenceService: PreferenceService

    @Inject
    lateinit var settingsStore: SettingsStore

    override fun buildComponentAndInject() {
        DaggerPreferenceComponent
                .builder()
                .dependencies(DaggerInjector.appComponent)
                .build()
                .inject(this)
    }

    fun onPreferenceUpdate(key: String, value: Int): Boolean {
        when (key) {
            Const.PREF_MAX_SIZE -> settingsStore.maxFileSizeForSearch.notify(value)
            Const.PREF_EXPLORER_ITEM -> settingsStore.explorerItem.pushByOriginal(value)
            else -> throw Exception()
        }
        return true
    }

    fun onPreferenceUpdate(key: String, value: String): Boolean {
        when (key) {
            Const.PREF_STORAGE_PATH -> settingsStore.storagePath.notify(value)
            Const.PREF_EXTRA_FORMATS -> settingsStore.extraFormats.notifyByOriginal(value)
            Const.PREF_SPECIAL_CHARACTERS -> settingsStore.specialCharacters.notifyByOriginal(value.trim())
            Const.PREF_APP_THEME -> settingsStore.appTheme.notifyByOriginal(value)
            Const.PREF_APP_ORIENTATION -> settingsStore.appOrientation.notifyByOriginal(value)
            else -> throw Exception()
        }
        return true
    }

    fun onPreferenceUpdate(key: String, value: Boolean): Boolean {
        when (key) {
            Const.PREF_USE_SU -> return onUpdateUseSu(value)
            else -> throw Exception()
        }
    }

    fun getCurrentValue(key: String): Any? = settingsStore.getCurrentValue(key)

    fun exportPreferences() {
        val output = preferenceService.exportPreferences()
        showOutput(output, R.string.successful)
    }

    fun exportHistory() {
        val output = preferenceService.exportHistory()
        showOutput(output, R.string.successful)
    }

    fun importPreferences() {
        val output = preferenceService.importPreferences()
        showOutput(output, R.string.successful_with_restart)
    }

    fun importHistory() {
        val output = preferenceService.importHistory()
        showOutput(output, R.string.successful)
        if (output.success) {
            PreferencesChannel.historyImportedEvent.justNotify()
        }
    }

    private fun showOutput(output: Shell.Output, successMessage: Int) {
        when {
            output.success -> alertOutputSuccess.invoke(successMessage)
            else -> alertOutputError.invoke(output)
        }
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    private fun onUpdateUseSu(newValue: Boolean): Boolean {
        val allowed = when {
            newValue -> {
                val output = Shell.checkSu()
                when {
                    output.success -> Unit
                    output.error.isNotBlank() -> alert.invoke(output.error)
                    else -> {
                        val message = app.getString(R.string.not_allowed)
                        alert.invoke(message)
                    }
                }
                output.success
            }
            else -> true
        }

        if (allowed) {
            settingsStore.useSu.notify(newValue)
        }
        return allowed
    }
}