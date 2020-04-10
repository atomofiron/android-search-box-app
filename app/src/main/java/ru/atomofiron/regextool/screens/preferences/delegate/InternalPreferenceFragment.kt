package ru.atomofiron.regextool.screens.preferences.delegate

import android.os.Bundle
import android.view.View
import androidx.preference.*
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.arch.view.Backable
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Util

internal class InternalPreferenceFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener, Backable {
    companion object {
        private const val DOES_NOT_MATTER = true
    }
    private lateinit var output: Output
    private lateinit var provider: Provider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(androidx.preference.R.id.recycler_view)
        recyclerView.clipToPadding = false
        val padding = resources.getDimensionPixelSize(R.dimen.joystick_size)
        recyclerView.setPadding(recyclerView.paddingLeft, recyclerView.paddingTop, recyclerView.paddingRight, padding)
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
        return when (val key = preference.key) {
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
            Const.PREF_EXPLORER_ITEM -> {
                preference.setOnPreferenceClickListener {
                    output.onExplorerItemClick()
                    true
                }
                DOES_NOT_MATTER
            }
            Const.PREF_MAX_DEPTH -> DOES_NOT_MATTER
            Const.PREF_EXCLUDE_DIRS -> DOES_NOT_MATTER
            Const.PREF_ESC_COLOR -> {
                preference.setOnPreferenceClickListener {
                    output.onEscColorClick()
                    true
                }
                DOES_NOT_MATTER
            }
            Const.PREF_LEAK_CANARY -> {
                preference as SwitchPreference
                preference.isChecked = provider.getCurrentValue(preference.key) as Boolean
                preference.setOnPreferenceClickListener {
                    output.onLeakCanaryClick()
                    true
                }
                DOES_NOT_MATTER
            }
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
        fun onExplorerItemClick()
        fun onEscColorClick()
        fun onLeakCanaryClick()
        fun onPreferenceUpdate(key: String, value: Int): Boolean
        fun onPreferenceUpdate(key: String, value: String): Boolean
        fun onPreferenceUpdate(key: String, value: Boolean): Boolean
    }
}