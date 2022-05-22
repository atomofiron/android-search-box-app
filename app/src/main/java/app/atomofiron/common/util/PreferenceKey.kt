package app.atomofiron.common.util

import android.content.SharedPreferences

sealed class PreferenceKey<T>(
    val name: String,
) {
    companion object {
        fun boolean(name: String): PreferenceKey<Boolean> = KeyBoolean(name)
        fun int(name: String): PreferenceKey<Int> = KeyInt(name)
        fun long(name: String): PreferenceKey<Long> = KeyLong(name)
        fun float(name: String): PreferenceKey<Float> = KeyFloat(name)
        fun string(name: String): PreferenceKey<String> = KeyString(name)
        fun stringSet(name: String): PreferenceKey<Set<String>> = KeyStringSet(name)
    }

    override fun toString(): String = name

    override fun equals(other: Any?): Boolean = when (other) {
        null -> false
        !is PreferenceKey<*> -> false
        else -> other.name == name
    }

    override fun hashCode(): Int = name.hashCode()

    abstract fun getValue(preferences: SharedPreferences, default: T?): T?
    abstract fun putValue(preferences: SharedPreferences, value: T)

    fun exists(preferences: SharedPreferences): Boolean = preferences.contains(name)

    fun remove(preferences: SharedPreferences) = preferences.edit().remove(name).apply()

    private class KeyBoolean(name: String) : PreferenceKey<Boolean>(name) {
        override fun getValue(preferences: SharedPreferences, default: Boolean?): Boolean? = when {
            preferences.contains(name) -> preferences.getBoolean(name, default ?: false)
            else -> default
        }

        override fun putValue(preferences: SharedPreferences, value: Boolean) = preferences.edit().putBoolean(name, value).apply()
    }
    private class KeyInt(name: String) : PreferenceKey<Int>(name) {
        override fun getValue(preferences: SharedPreferences, default: Int?): Int? = when {
            preferences.contains(name) -> preferences.getInt(name, default ?: 0)
            else -> default
        }

        override fun putValue(preferences: SharedPreferences, value: Int) = preferences.edit().putInt(name, value).apply()
    }
    private class KeyLong(name: String) : PreferenceKey<Long>(name) {
        override fun getValue(preferences: SharedPreferences, default: Long?): Long? = when {
            preferences.contains(name) -> preferences.getLong(name, default ?: 0L)
            else -> default
        }

        override fun putValue(preferences: SharedPreferences, value: Long) = preferences.edit().putLong(name, value).apply()
    }
    private class KeyFloat(name: String) : PreferenceKey<Float>(name) {
        override fun getValue(preferences: SharedPreferences, default: Float?): Float? = when {
            preferences.contains(name) -> preferences.getFloat(name, default ?: 0f)
            else -> default
        }

        override fun putValue(preferences: SharedPreferences, value: Float) = preferences.edit().putFloat(name, value).apply()
    }
    private class KeyString(name: String) : PreferenceKey<String>(name) {
        override fun getValue(preferences: SharedPreferences, default: String?): String? = when {
            preferences.contains(name) -> preferences.getString(name, default)
            else -> default
        }

        override fun putValue(preferences: SharedPreferences, value: String) = preferences.edit().putString(name, value).apply()
    }
    private class KeyStringSet(name: String) : PreferenceKey<Set<String>>(name) {
        override fun getValue(preferences: SharedPreferences, default: Set<String>?): Set<String>? = when {
            preferences.contains(name) -> preferences.getStringSet(name, default)
            else -> default
        }

        override fun putValue(preferences: SharedPreferences, value: Set<String>) = preferences.edit().putStringSet(name, value).apply()
    }
}

fun <T> SharedPreferences.get(key: PreferenceKey<T>, default: T?): T? = key.getValue(this, default)

fun <T> SharedPreferences.put(key: PreferenceKey<T>, value: T?) = when (value) {
    null -> key.remove(this)
    else -> key.putValue(this, value)
}