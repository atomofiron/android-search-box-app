package app.atomofiron.searchboxapp.screens.preferences.fragment

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import androidx.preference.*
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.preference.TextFieldPreference
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.screens.preferences.PreferenceFragment
import app.atomofiron.searchboxapp.screens.preferences.PreferenceViewModel
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.PreferenceKeys
import app.atomofiron.searchboxapp.utils.Util.toHumanReadable

class PreferenceFragmentDelegate(
    private val fragment: PreferenceFragment,
    private val viewModel: PreferenceViewModel,
    private val clickOutput: PreferenceClickOutput,
) : Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private val resources get() = fragment.resources

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        return onUpdatePreference(preference, newValue)
    }

    fun onCreatePreference(preference: Preference) {
        setPreferenceListeners(preference)
        when (preference) {
            is PreferenceGroup -> {
                for (i in 0 until preference.preferenceCount) {
                    onCreatePreference(preference[i])
                }
            }
        }
    }

    private fun setPreferenceListeners(preference: Preference) {
        preference.onPreferenceChangeListener = this
        preference.onPreferenceClickListener = this

        when (preference.key) {
            PreferenceKeys.KEY_SPECIAL_CHARACTERS.name -> {
                preference as TextFieldPreference
                preference.setFilter {
                    it.replace(Regex("[ ]+"), Const.SPACE).trim()
                }
            }
        }
    }

    private fun onUpdatePreference(preference: Preference, newValue: Any?): Boolean {
        when (val key = preference.key) {
            PreferenceKeys.KEY_STORAGE_PATH.name -> {
                if (newValue !is String || newValue.isBlank()) {
                    return false
                }
                preference.updateStringSummary(newValue as String?)
            }
            PreferenceKeys.KEY_TEXT_FORMATS.name -> preference.updateStringSummary(newValue as String?)
            PreferenceKeys.KEY_SPECIAL_CHARACTERS.name -> preference.updateStringSummary(newValue as String?)
            PreferenceKeys.KEY_APP_THEME.name -> {
                var name = newValue?.toString() ?: preference.preferenceDataStore?.getString(key, null)
                name = AppTheme.fromString(name).name
                val index = resources.getStringArray(R.array.theme_val).indexOf(name)
                preference.summary = resources.getStringArray(R.array.theme_var)[index]
            }
            PreferenceKeys.KEY_APP_ORIENTATION.name -> {
                val i = (newValue?.toString() ?: preference.preferenceDataStore?.getString(key, null))?.toInt() ?: 0
                preference.summary = resources.getStringArray(R.array.orientation_var)[i]
            }
            PreferenceKeys.KEY_MAX_SIZE.name -> preference.updateMaxSize(newValue)
            PreferenceKeys.PREF_EXPORT_IMPORT -> preference.isEnabled = viewModel.isExportImportAvailable
            PreferenceKeys.KEY_TOYBOX.name -> preference.isVisible = SDK_INT < Q
        }
        return true
    }

    private fun Preference.updateMaxSize(newValue: Any?) {
        if (newValue is Long) {
            val suffixes = resources.getStringArray(R.array.size_suffix_arr)
            summary = newValue.toHumanReadable(suffixes)
        }
    }

    private fun Preference.updateStringSummary(newValue: String?) {
        summary = newValue ?: preferenceDataStore?.getString(key, null)
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.key) {
            PreferenceKeys.PREF_ABOUT -> clickOutput.onAboutClick()
            PreferenceKeys.PREF_EXPORT_IMPORT -> clickOutput.onExportImportClick()
            PreferenceKeys.KEY_EXPLORER_ITEM.name -> clickOutput.onExplorerItemClick()
            PreferenceKeys.KEY_JOYSTICK.name -> clickOutput.onJoystickClick()
            PreferenceKeys.KEY_TOYBOX.name -> clickOutput.onToyboxClick()
        }
        return true
    }
}