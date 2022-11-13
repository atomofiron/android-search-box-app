package app.atomofiron.searchboxapp.screens.preferences.fragment

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.preference.PreferenceDataStore
import app.atomofiron.searchboxapp.injectable.store.PreferencesStore
import app.atomofiron.searchboxapp.poop
import kotlinx.coroutines.*

class LegacyPreferenceDataStore(
    preferencesStore: PreferencesStore,
    private val scope: CoroutineScope,
) : PreferenceDataStore(), DataStore<Preferences> by preferencesStore {

    private var preferences: Preferences = preferencesStore.initialPreferences

    init {
        launchImmediately {
            preferencesStore.data.collect {
                preferences = it
            }
        }
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferences[booleanPreferencesKey(key)] ?: defValue
    }

    override fun getInt(key: String, defValue: Int): Int {
        return preferences[intPreferencesKey(key)] ?: defValue
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return preferences[floatPreferencesKey(key)] ?: defValue
    }

    override fun getLong(key: String, defValue: Long): Long {
        return preferences[longPreferencesKey(key)] ?: defValue
    }

    override fun getString(key: String, defValue: String?): String? {
        poop("getString $key ${preferences[stringPreferencesKey(key)]}")
        return preferences[stringPreferencesKey(key)] ?: defValue
    }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        return preferences[stringSetPreferencesKey(key)] ?: defValues
    }

    override fun putBoolean(key: String, value: Boolean) {
        launchImmediately {
            edit {
                it[booleanPreferencesKey(key)] = value
            }
        }
    }

    override fun putInt(key: String, value: Int) {
        launchImmediately {
            edit {
                it[intPreferencesKey(key)] = value
            }
        }
    }

    override fun putFloat(key: String, value: Float) {
        launchImmediately {
            edit {
                it[floatPreferencesKey(key)] = value
            }
        }
    }

    override fun putLong(key: String, value: Long) {
        launchImmediately {
            edit {
                it[longPreferencesKey(key)] = value
            }
        }
    }

    override fun putString(key: String, value: String?) {
        poop("getString $key $value")
        val pKey = stringPreferencesKey(key)
        launchImmediately {
            edit {
                when (value) {
                    null -> it.remove(pKey)
                    else -> it[pKey] = value
                }
            }
        }
    }

    override fun putStringSet(key: String, values: Set<String>?) {
        val pKey = stringSetPreferencesKey(key)
        launchImmediately {
            edit {
                when (values) {
                    null -> it.remove(pKey)
                    else -> it[pKey] = values
                }
            }
        }
    }

    private fun launchImmediately(block: suspend CoroutineScope.() -> Unit) {
        scope.launch(Dispatchers.Main.immediate, start = CoroutineStart.UNDISPATCHED, block)
    }
}