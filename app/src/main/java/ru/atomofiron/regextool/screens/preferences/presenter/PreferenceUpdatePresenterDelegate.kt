package ru.atomofiron.regextool.screens.preferences.presenter

import android.content.Context
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.injectable.store.SettingsStore
import ru.atomofiron.regextool.screens.preferences.PreferenceViewModel
import ru.atomofiron.regextool.screens.preferences.fragment.PreferenceUpdateOutput
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Shell

class PreferenceUpdatePresenterDelegate(
        private val context: Context,
        private val viewModel: PreferenceViewModel,
        private val settingsStore: SettingsStore
) : PreferenceUpdateOutput {

    override fun onPreferenceUpdate(key: String, value: Int) {
        when (key) {
            Const.PREF_MAX_SIZE -> settingsStore.maxFileSizeForSearch.notify(value)
            Const.PREF_EXPLORER_ITEM -> settingsStore.explorerItemComposition.pushByOriginal(value)
            else -> throw Exception()
        }
    }

    override fun onPreferenceUpdate(key: String, value: String) {
        when (key) {
            Const.PREF_STORAGE_PATH -> settingsStore.storagePath.notify(value)
            Const.PREF_EXTRA_FORMATS -> settingsStore.extraFormats.notifyByOriginal(value)
            Const.PREF_SPECIAL_CHARACTERS -> settingsStore.specialCharacters.notifyByOriginal(value.trim())
            Const.PREF_APP_THEME -> settingsStore.appTheme.notifyByOriginal(value)
            Const.PREF_APP_ORIENTATION -> settingsStore.appOrientation.notifyByOriginal(value)
            else -> throw Exception()
        }
    }

    override fun onPreferenceUpdate(key: String, value: Boolean): Boolean {
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
                    output.error.isNotBlank() -> viewModel.alert.invoke(output.error)
                    else -> {
                        val message = context.getString(R.string.not_allowed)
                        viewModel.alert.invoke(message)
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