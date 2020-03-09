package ru.atomofiron.regextool.screens.preferences

import android.app.Application
import app.atomofiron.common.base.BaseViewModel
import app.atomofiron.common.util.SingleLiveEvent
import com.google.android.material.snackbar.Snackbar
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.iss.store.SettingsStore
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Shell
import java.lang.Exception

class PreferenceViewModel(app: Application) : BaseViewModel<PreferenceRouter>(app) {
    override val router = PreferenceRouter()

    val warning = SingleLiveEvent<String>()

    fun onPreferenceUpdate(key: String, value: Int): Boolean {
        when (key) {
            Const.PREF_MAX_SIZE -> SettingsStore.maxFileSizeForSearch.notify(value)
            else -> throw Exception()
        }
        return true
    }

    fun onPreferenceUpdate(key: String, value: String): Boolean {
        when (key) {
            Const.PREF_STORAGE_PATH -> SettingsStore.storagePath.notify(value)
            Const.PREF_EXTRA_FORMATS -> SettingsStore.extraFormats.notifyByOriginal(value)
            Const.PREF_SPECIAL_CHARACTERS -> SettingsStore.specialCharacters.notifyByOriginal(value.trim())
            Const.PREF_APP_THEME -> SettingsStore.appTheme.notifyByOriginal(value)
            Const.PREF_APP_ORIENTATION -> SettingsStore.appOrientation.notifyByOriginal(value)
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

    @Suppress("IMPLICIT_CAST_TO_ANY")
    private fun onUpdateUseSu(newValue: Boolean): Boolean {
        val allowed = when {
            newValue -> {
                val output = Shell.checkSu()
                when {
                    output.success -> Unit
                    output.error.isNotBlank() -> warning.invoke(output.error)
                    else -> {
                        val message = app.getString(R.string.not_allowed)
                        warning.invoke(message)
                    }
                }
                output.success
            }
            else -> true
        }

        if (allowed) {
            SettingsStore.useSu.notify(newValue)
        }
        return allowed
    }
}