package ru.atomofiron.regextool.screens.preferences.presenter

import android.content.Context
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.screens.preferences.PreferenceViewModel
import ru.atomofiron.regextool.screens.preferences.fragment.PreferenceUpdateOutput
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Shell

class PreferenceUpdatePresenterDelegate(
        private val context: Context,
        private val viewModel: PreferenceViewModel,
        private val preferenceStore: PreferenceStore
) : PreferenceUpdateOutput {

    override fun onPreferenceUpdate(key: String, value: Int) {
        when (key) {
            Const.PREF_MAX_DEPTH -> preferenceStore.maxDepthForSearch.notifyByOriginal(value)
            Const.PREF_EXPLORER_ITEM -> preferenceStore.explorerItemComposition.pushByOriginal(value)
            else -> throw Exception()
        }
    }

    override fun onPreferenceUpdate(key: String, value: Long) {
        when (key) {
            Const.PREF_MAX_SIZE -> preferenceStore.maxFileSizeForSearch.notify(value)
            else -> throw Exception()
        }
    }

    override fun onPreferenceUpdate(key: String, value: String) {
        when (key) {
            Const.PREF_STORAGE_PATH -> preferenceStore.storagePath.notify(value)
            Const.PREF_TEXT_FORMATS -> preferenceStore.textFormats.notifyByOriginal(value)
            Const.PREF_SPECIAL_CHARACTERS -> preferenceStore.specialCharacters.notifyByOriginal(value.trim())
            Const.PREF_APP_THEME -> preferenceStore.appTheme.notifyByOriginal(value)
            Const.PREF_APP_ORIENTATION -> preferenceStore.appOrientation.notifyByOriginal(value)
            else -> throw Exception()
        }
    }

    override fun onPreferenceUpdate(key: String, value: Boolean): Boolean {
        when (key) {
            Const.PREF_EXCLUDE_DIRS -> preferenceStore.excludeDirs.notifyByOriginal(value)
            Const.PREF_USE_SU -> return onUpdateUseSu(value)
            else -> throw Exception()
        }
        return true
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
            preferenceStore.useSu.notify(newValue)
        }
        return allowed
    }
}