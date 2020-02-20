package ru.atomofiron.regextool.screens.preferences

import android.os.Bundle
import android.view.*
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.iss.store.SettingsStore
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Shell.checkSu
import ru.atomofiron.regextool.utils.Util
import java.util.*

class PrefsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_preferences, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.tips) {
            Util.showHelp(context)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val prefScreen = preferenceScreen
        for (i in 0 until prefScreen.preferenceCount) {
            var pref = prefScreen.getPreference(i)
            if (pref.javaClass == PreferenceCategory::class.java) {
                val prefCategory = pref as PreferenceCategory
                for (j in 0 until prefCategory.preferenceCount) {
                    pref = prefCategory.getPreference(j)
                    pref.onPreferenceChangeListener = this
                    updatePreference(pref, null)
                }
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) = Unit

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        return updatePreference(preference, newValue)
    }

    private fun updatePreference(pref: Preference, newValue: Any?): Boolean {
        when (pref.key) {
            Const.PREF_STORAGE_PATH -> {
                pref.summary = newValue as? String
                if (newValue is String) {
                    SettingsStore.storagePath.notify(newValue)
                }
            }
            Const.PREF_EXTRA_FORMATS -> {
                pref.summary = newValue as? String
                if (newValue is String) {
                    SettingsStore.extraFormats.notify(newValue)
                }
            }
            Const.PREF_SPECIAL_CHARACTERS -> {
                pref.summary = newValue as? String
                if (newValue is String) {
                    SettingsStore.specialCharacters.notify(newValue)
                }
            }
            Const.PREF_APP_THEME -> {
                val i = newValue as? Int ?: SettingsStore.appTheme.value.ordinal
                pref.summary = resources.getStringArray(R.array.theme_var)[i]
                if (newValue is Int) {
                    SettingsStore.appTheme.notifyByOriginal(i)
                }
            }
            Const.PREF_APP_ORIENTATION -> {
                val i = newValue as? Int ?: SettingsStore.appOrientation.value.ordinal
                pref.summary = resources.getStringArray(R.array.orientation_var)[i]
                if (newValue is Int) {
                    SettingsStore.appOrientation.notifyByOriginal(i)
                }
            }
            Const.PREF_USE_SU -> {
                val use = newValue as? Boolean ?: SettingsStore.useSu.value
                val allow = !use || checkSu()
                if (allow && newValue is Boolean) {
                    SettingsStore.useSu.notify(newValue)
                }
                return allow
            }
            Const.PREF_MAX_SIZE -> {
                val maxSize = newValue ?: SettingsStore.maxFileSizeForSearch.value
                val view = view
                if (view != null) {
                    val intValue = maxSize as Int
                    view.post(Runnable {
                        // иначе вьюха меняется в процессе рассчётов списка
                        val suffixes = resources.getStringArray(R.array.size_suffix_arr)
                        pref.summary = Util.intToHumanReadable(intValue, suffixes)
                    })
                }
                if (newValue is Int) {
                    SettingsStore.maxFileSizeForSearch.notify(newValue)
                }
            }
        }
        return true
    }
}