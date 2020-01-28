package ru.atomofiron.regextool.screens.preferences

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.utils.Shell.checkSu
import ru.atomofiron.regextool.utils.Util
import java.util.*

class PrefsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
    companion object {
        val changedPrefs = ArrayList<String>()
    }
    private var sp: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        setHasOptionsMenu(true)
        sp = Util.sp(activity)
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
                    update(pref, null)
                }
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) = Unit

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val result = update(preference, newValue)
        when (preference.key) {
            Util.PREF_THEME, Util.PREF_ORIENTATION -> activity!!.recreate()
            Util.PREF_MAX_SIZE -> update(preference, newValue)
        }
        // чтобы можно было проверить факт изменения определённой конфигурации
        // и совершить необходимые действия
        // todo переделать по-человечески
        when (val key = preference.key) {
            Util.PREF_SPECIAL_CHARACTERS -> changedPrefs.add(key)
        }
        return result
    }

    private fun update(pref: Preference, newValue: Any?): Boolean {
        val value = newValue?.toString()
        when (val key = pref.key) {
            Util.PREF_STORAGE_PATH, Util.PREF_EXTRA_FORMATS -> {
                pref.summary = value ?: sp!!.getString(key, "")
            }
            Util.PREF_SPECIAL_CHARACTERS -> {
                pref.summary = value ?: sp!!.getString(Util.PREF_SPECIAL_CHARACTERS, Util.DEFAULT_SPECIAL_CHARACTERS)
            }
            Util.PREF_THEME -> {
                val i = value?.toInt() ?: sp!!.getString(key, "0")!!.toInt()
                pref.summary = resources.getStringArray(R.array.theme_var)[i]
            }
            Util.PREF_ORIENTATION -> {
                val i = value?.toInt() ?: sp!!.getString(key, "2")!!.toInt()
                pref.summary = resources.getStringArray(R.array.orientation_var)[i]
            }
            Util.PREF_USE_SU -> when (value) {
                null -> if (sp!!.getBoolean(key, false) && !checkSu()) (pref as SwitchPreferenceCompat).isChecked = false
                "true" -> return checkSu()
            }
            Util.PREF_MAX_SIZE -> {
                val maxSize = newValue ?: sp!!.getInt(key, 0)
                val view = view
                if (view != null) {
                    val intValue = maxSize as Int
                    view.post(Runnable {
                        // иначе вьюха меняется в процессе рассчётов списка
                        val suffixes = resources.getStringArray(R.array.size_suffix_arr)
                        pref.summary = Util.intToHumanReadable(intValue, suffixes)
                    })
                }
            }
        }
        return true
    }
}