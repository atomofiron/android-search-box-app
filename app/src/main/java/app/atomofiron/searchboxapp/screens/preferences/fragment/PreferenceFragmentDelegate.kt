package app.atomofiron.searchboxapp.screens.preferences.fragment

import android.os.Build
import androidx.preference.*
import app.atomofiron.searchboxapp.BuildConfig
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.preference.TextFieldPreference
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.screens.preferences.PreferenceFragment
import app.atomofiron.searchboxapp.screens.preferences.PreferenceViewModel
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.Util

class PreferenceFragmentDelegate(
    private val fragment: PreferenceFragment,
    private val viewModel: PreferenceViewModel,
    private val output: PreferenceUpdateOutput
) : Preference.OnPreferenceChangeListener {
    companion object {
        private const val DOES_NOT_MATTER = true
    }

    private val resources = fragment.resources

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        return onUpdatePreference(preference, newValue)
    }

    fun onUpdateScreen(screen: PreferenceScreen) {
        for (i in 0 until screen.preferenceCount) {
            var preference = screen.getPreference(i)
            when (preference) {
                is PreferenceScreen -> onUpdateScreen(preference)
                is PreferenceCategory -> {
                    val preferenceCategory = preference
                    if (!BuildConfig.DEBUG && preferenceCategory.key == Const.PREF_CATEGORY_DEBUG) {
                        preferenceCategory.isVisible = false
                    } else {
                        for (j in 0 until preferenceCategory.preferenceCount) {
                            preference = preferenceCategory.getPreference(j)
                            preference.onPreferenceChangeListener = this
                            onUpdatePreference(preference, null)
                        }
                    }
                }
                is Preference -> {
                    preference.onPreferenceChangeListener = this
                    onUpdatePreference(preference, null)
                }
            }
        }
    }

    private fun onUpdatePreference(preference: Preference, newValue: Any?): Boolean {
        return when (val key = preference.key) {
            Const.PREF_ABOUT -> {
                preference.setOnPreferenceClickListener {
                    fragment.onAboutClick()
                    true
                }
                DOES_NOT_MATTER
            }
            Const.PREF_STORAGE_PATH -> {
                if (newValue is String && newValue.isBlank()) {
                    return false
                }
                preference.summary = newValue as? String ?: viewModel.getCurrentValue(key) as String
                if (newValue is String) {
                    output.onPreferenceUpdate(key, newValue)
                }
                true
            }
            Const.PREF_TEXT_FORMATS -> {
                preference.summary = newValue as? String ?: viewModel.getCurrentValue(key) as String
                if (newValue is String) {
                    output.onPreferenceUpdate(key, newValue)
                }
                DOES_NOT_MATTER
            }
            Const.PREF_SPECIAL_CHARACTERS -> {
                preference.summary = newValue as? String ?: viewModel.getCurrentValue(key) as String
                when (newValue) {
                    is String -> output.onPreferenceUpdate(key, newValue)
                    else -> {
                        preference as TextFieldPreference
                        preference.setFilter {
                            it.replace(Regex("[ ]+"), Const.SPACE).trim()
                        }
                    }
                }
                DOES_NOT_MATTER
            }
            Const.PREF_APP_THEME -> {
                var name = newValue as? String ?: viewModel.getCurrentValue(key) as String
                name = AppTheme.fromString(name).name
                val index = resources.getStringArray(R.array.theme_val).indexOf(name)
                preference.summary = resources.getStringArray(R.array.theme_var)[index]
                if (newValue is String) {
                    output.onPreferenceUpdate(key, newValue)
                }
                DOES_NOT_MATTER
            }
            Const.PREF_APP_ORIENTATION -> {
                val i = (newValue as? String ?: viewModel.getCurrentValue(key) as String).toInt()
                preference.summary = resources.getStringArray(R.array.orientation_var)[i]
                if (newValue is String) {
                    output.onPreferenceUpdate(key, newValue)
                }
                DOES_NOT_MATTER
            }
            Const.PREF_USE_SU -> {
                newValue ?: return DOES_NOT_MATTER
                when (newValue) {
                    is Boolean -> output.onPreferenceUpdate(key, newValue)
                    else -> DOES_NOT_MATTER
                }
            }
            Const.PREF_MAX_SIZE -> {
                when (newValue) {
                    !is Int -> return DOES_NOT_MATTER
                    else -> output.onPreferenceUpdate(preference.key, newValue)
                }
                onUpdateMaxSize(preference, newValue)
                return DOES_NOT_MATTER
            }
            Const.PREF_EXPORT_IMPORT -> {
                preference.isEnabled = viewModel.isExportImportAvailable
                preference.setOnPreferenceClickListener {
                    fragment.onExportImportClick()
                    true
                }
                DOES_NOT_MATTER
            }
            Const.PREF_EXPLORER_ITEM -> {
                preference.setOnPreferenceClickListener {
                    fragment.onExplorerItemClick()
                    true
                }
                DOES_NOT_MATTER
            }
            Const.PREF_MAX_DEPTH -> {
                newValue ?: return DOES_NOT_MATTER
                output.onPreferenceUpdate(key, newValue as Int)
                DOES_NOT_MATTER
            }
            Const.PREF_EXCLUDE_DIRS -> {
                newValue ?: return DOES_NOT_MATTER
                output.onPreferenceUpdate(key, newValue as Boolean)
                DOES_NOT_MATTER
            }
            Const.PREF_JOYSTICK -> {
                preference.setOnPreferenceClickListener {
                    fragment.onJoystickClick()
                    true
                }
                DOES_NOT_MATTER
            }
            Const.PREF_TOYBOX -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    preference.isVisible = false
                } else {
                    preference.setOnPreferenceClickListener {
                        fragment.onToyboxClick()
                        true
                    }
                }
                DOES_NOT_MATTER
            }
            Const.PREF_DEEP_BLACK -> true
            Const.PREF_LEAK_CANARY -> {
                preference as SwitchPreferenceCompat
                preference.isChecked = viewModel.getCurrentValue(preference.key) as Boolean
                preference.setOnPreferenceClickListener {
                    it as SwitchPreferenceCompat
                    fragment.onLeakCanaryClick(it.isChecked)
                    true
                }
                DOES_NOT_MATTER
            }
            else -> throw Exception("Unknown preference ($key)!")
        }
    }

    private fun onUpdateMaxSize(preference: Preference, newValue: Any?) {
        val maxSize = newValue ?: viewModel.getCurrentValue(preference.key)
        if (fragment.view != null) {
            val intValue = maxSize as Int
            val suffixes = resources.getStringArray(R.array.size_suffix_arr)
            preference.summary = Util.intToHumanReadable(intValue, suffixes)
        }
    }
}