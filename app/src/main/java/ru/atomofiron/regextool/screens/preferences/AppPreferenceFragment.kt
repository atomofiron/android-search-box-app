package ru.atomofiron.regextool.screens.preferences

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.base.Backable
import com.google.android.material.snackbar.Snackbar
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.iss.store.SettingsStore
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Shell.checkSu
import ru.atomofiron.regextool.utils.Util

class AppPreferenceFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener, Backable {
    private val anchorView: View get() = activity!!.findViewById(R.id.root_iv_joystick)
    private lateinit var output: Output

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) = Unit

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
        onUpdateScreen(preferenceScreen)
        return super.onCreateAdapter(preferenceScreen)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        return onUpdatePreference(preference, newValue)
    }

    override fun onBack(): Boolean = output.onBack()

    fun setAppPreferenceFragmentOutput(output: Output) {
        this.output = output
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
                    SettingsStore.extraFormats.notifyByOriginal(newValue)
                }
            }
            Const.PREF_SPECIAL_CHARACTERS -> {
                preference.summary = newValue as? String ?: SettingsStore.specialCharacters.value
                if (newValue is String) {
                    SettingsStore.specialCharacters.notifyByOriginal(newValue.trim())
                }
            }
            Const.PREF_APP_THEME -> {
                val i = (newValue as? String ?: SettingsStore.appTheme.value).toInt()
                preference.summary = resources.getStringArray(R.array.theme_var)[i]
                if (newValue is String) {
                    SettingsStore.appTheme.notifyByOriginal(newValue)
                }
            }
            Const.PREF_APP_ORIENTATION -> {
                val i = (newValue as? String ?: SettingsStore.appOrientation.value).toInt()
                preference.summary = resources.getStringArray(R.array.orientation_var)[i]
                if (newValue is String) {
                    SettingsStore.appOrientation.notifyByOriginal(newValue)
                }
            }
            Const.PREF_USE_SU -> {
                newValue ?: return true
                newValue as Boolean
                return onUpdateUseSu(newValue)
            }
            Const.PREF_MAX_SIZE -> onUpdateMaxSize(preference, newValue)
            Const.PREF_EXPORT_IMPORT -> {
                preference.isEnabled = ExportImportDelegate.isAvailable
                preference.setOnPreferenceClickListener {
                    output.onExportImportClick()
                    true
                }
            }
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

    interface Output: Backable {
        override fun onBack(): Boolean
        fun onExportImportClick()
    }
}