package ru.atomofiron.regextool.screens.preferences

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.base.Backable
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Util
import java.lang.Exception

internal class InternalPreferenceFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener, Backable {
    companion object {
        private const val DOES_NOT_MATTER = true
    }
    private lateinit var output: Output
    private lateinit var provider: Provider
    private lateinit var sp: SharedPreferences

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

    fun setAppPreferenceFragmentProvider(provider: Provider) {
        this.provider = provider
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
        val key = preference.key
        return when (key) {
            Const.PREF_STORAGE_PATH -> {
                preference.summary = newValue as? String ?: provider.getCurrentValue(key) as String
                when (newValue) {
                    is String -> output.onPreferenceUpdate(key, newValue)
                    else -> DOES_NOT_MATTER
                }
            }
            Const.PREF_EXTRA_FORMATS -> {
                preference.summary = newValue as? String ?: provider.getCurrentValue(key) as String
                when (newValue) {
                    is String -> output.onPreferenceUpdate(key, newValue)
                    else -> DOES_NOT_MATTER
                }
            }
            Const.PREF_SPECIAL_CHARACTERS -> {
                preference.summary = newValue as? String ?: provider.getCurrentValue(key) as String
                when (newValue) {
                    is String -> output.onPreferenceUpdate(key, newValue)
                    else -> DOES_NOT_MATTER
                }
            }
            Const.PREF_APP_THEME -> {
                val i = (newValue as? String ?: provider.getCurrentValue(key) as String).toInt()
                preference.summary = resources.getStringArray(R.array.theme_var)[i]
                when (newValue) {
                    is String -> output.onPreferenceUpdate(key, newValue)
                    else -> DOES_NOT_MATTER
                }
            }
            Const.PREF_APP_ORIENTATION -> {
                val i = (newValue as? String ?: provider.getCurrentValue(key) as String).toInt()
                preference.summary = resources.getStringArray(R.array.orientation_var)[i]
                when (newValue) {
                    is String -> output.onPreferenceUpdate(key, newValue)
                    else -> DOES_NOT_MATTER
                }
            }
            Const.PREF_USE_SU -> {
                newValue ?: return DOES_NOT_MATTER
                when (newValue) {
                    is Boolean -> output.onPreferenceUpdate(key, newValue)
                    else -> DOES_NOT_MATTER
                }
            }
            Const.PREF_MAX_SIZE -> {
                val allowed = when (newValue) {
                    !is Int -> DOES_NOT_MATTER
                    else -> output.onPreferenceUpdate(preference.key, newValue)
                }
                if (allowed) {
                    onUpdateMaxSize(preference, newValue)
                }
                return allowed
            }
            Const.PREF_EXPORT_IMPORT -> {
                preference.isEnabled = provider.isExportImportAvailable
                preference.setOnPreferenceClickListener {
                    output.onExportImportClick()
                    true
                }
                DOES_NOT_MATTER
            }
            Const.PREF_MAX_DEPTH -> DOES_NOT_MATTER
            Const.PREF_EXCLUDE_DIRS -> DOES_NOT_MATTER
            else -> throw Exception("Unknown preference ($key)!")
        }
    }

    private fun onUpdateMaxSize(preference: Preference, newValue: Any?) {
        val maxSize = newValue ?: provider.getCurrentValue(preference.key)
        val view = view
        if (view != null) {
            val intValue = maxSize as Int
            val suffixes = resources.getStringArray(R.array.size_suffix_arr)
            preference.summary = Util.intToHumanReadable(intValue, suffixes)
        }
    }

    interface Provider {
        val isExportImportAvailable: Boolean
        fun getCurrentValue(key: String): Any?
    }

    interface Output : Backable {
        override fun onBack(): Boolean
        fun onExportImportClick()
        fun onPreferenceUpdate(key: String, value: Int): Boolean
        fun onPreferenceUpdate(key: String, value: String): Boolean
        fun onPreferenceUpdate(key: String, value: Boolean): Boolean
    }
}