package app.atomofiron.searchboxapp.screens.preferences.presenter

import android.content.Context
import app.atomofiron.common.util.flow.value
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.preferences.PreferenceViewModel
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceUpdateOutput
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.Shell

class PreferenceUpdatePresenterDelegate(
    private val context: Context,
    private val viewModel: PreferenceViewModel,
    private val preferenceStore: PreferenceStore,
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

    override fun onPreferenceUpdate(key: String, value: Boolean): Boolean {
        when (key) {
            Const.PREF_EXCLUDE_DIRS -> preferenceStore.excludeDirs.notifyByOriginal(value)
            Const.PREF_USE_SU -> return onUpdateUseSu(value)
            Const.PREF_DEEP_BLACK -> preferenceStore.deepBlack.notifyByOriginal(value)
            else -> throw Exception()
        }
        return true
    }

    override fun onPreferenceUpdate(key: String, value: String) {
        when (key) {
            Const.PREF_STORAGE_PATH -> preferenceStore.storagePath.notify(value)
            Const.PREF_TEXT_FORMATS -> preferenceStore.textFormats.notifyByOriginal(value)
            Const.PREF_SPECIAL_CHARACTERS -> preferenceStore.specialCharacters.notifyByOriginal(value)
            Const.PREF_APP_THEME -> preferenceStore.appTheme.notifyByOriginal(value)
            Const.PREF_APP_ORIENTATION -> preferenceStore.appOrientation.notifyByOriginal(value)
            else -> throw Exception()
        }
    }

    override fun onPreferenceUpdate(key: String, value: Set<String>) {
        when (key) {
            Const.PREF_TOYBOX -> preferenceStore.toyboxVariant.pushByOriginal(value)
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
                    output.error.isNotBlank() -> viewModel.alert.value = output.error
                    else -> {
                        val message = context.getString(R.string.not_allowed)
                        viewModel.alert.value = message
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