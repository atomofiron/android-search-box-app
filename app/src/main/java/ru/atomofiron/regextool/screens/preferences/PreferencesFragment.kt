package ru.atomofiron.regextool.screens.preferences

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.iss.store.SettingsStore
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Shell.checkSu
import ru.atomofiron.regextool.utils.Util

class PreferencesFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    private val anchorView: View get() = activity!!.findViewById(R.id.root_iv_joystick)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        setHasOptionsMenu(true)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) = Unit

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
        onUpdateScreen(preferenceScreen)
        return super.onCreateAdapter(preferenceScreen)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        return onUpdatePreference(preference, newValue)
    }

    private fun onUpdateScreen(screen: PreferenceScreen) {
        for (i in 0 until screen.preferenceCount) {
            var preference = screen.getPreference(i)
            when (preference) {
                is PreferenceScreen -> onUpdateScreen(preference)
                is PreferenceCategory -> {
                    val preferenceCategory = preference
                    for (j in 0 until preferenceCategory.preferenceCount) {
                        preference = preferenceCategory.getPreference(j)
                        preference.onPreferenceChangeListener = this
                        onUpdatePreference(preference, null)
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
        when (preference.key) {
            Const.PREF_STORAGE_PATH -> {
                preference.summary = newValue as? String ?: SettingsStore.storagePath.value
                if (newValue is String) {
                    SettingsStore.storagePath.notify(newValue)
                }
            }
            Const.PREF_EXTRA_FORMATS -> {
                preference.summary = newValue as? String ?: SettingsStore.extraFormats.value
                if (newValue is String) {
                    SettingsStore.extraFormats.notify(newValue)
                }
            }
            Const.PREF_SPECIAL_CHARACTERS -> {
                preference.summary = newValue as? String ?: SettingsStore.specialCharacters.value
                if (newValue is String) {
                    SettingsStore.specialCharacters.notify(newValue)
                }
            }
            Const.PREF_APP_THEME -> {
                val i = newValue as? Int ?: SettingsStore.appTheme.value.ordinal
                preference.summary = resources.getStringArray(R.array.theme_var)[i]
                if (newValue is Int) {
                    SettingsStore.appTheme.notifyByOriginal(i)
                }
            }
            Const.PREF_APP_ORIENTATION -> {
                val i = newValue as? Int ?: SettingsStore.appOrientation.value.ordinal
                preference.summary = resources.getStringArray(R.array.orientation_var)[i]
                if (newValue is Int) {
                    SettingsStore.appOrientation.notifyByOriginal(i)
                }
            }
            Const.PREF_USE_SU -> {
                newValue ?: return true
                newValue as Boolean
                return onUpdateUseSu(newValue)
            }
            Const.PREF_MAX_SIZE -> onUpdateMaxSize(preference, newValue)
        }
        return true
    }

    private fun onUpdateUseSu(newValue: Boolean): Boolean {
        val allowed = when {
            newValue -> {
                val output = checkSu()
                if (!output.success) {
                    val message = when {
                        output.error.isEmpty() -> getString(R.string.not_allowed)
                        else -> output.error
                    }
                    Snackbar
                            .make(view!!, message, Snackbar.LENGTH_SHORT)
                            .setAnchorView(anchorView)
                            .show()
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

    private fun onUpdateMaxSize(preference: Preference, newValue: Any?) {
        val maxSize = newValue ?: SettingsStore.maxFileSizeForSearch.value
        val view = view
        if (view != null) {
            val intValue = maxSize as Int
            val suffixes = resources.getStringArray(R.array.size_suffix_arr)
            preference.summary = Util.intToHumanReadable(intValue, suffixes)
        }
        if (newValue is Int) {
            SettingsStore.maxFileSizeForSearch.notify(newValue)
        }
    }
}