package ru.atomofiron.regextool.injectable.store.util

import android.content.SharedPreferences
import app.atomofiron.common.util.KObservable

class PreferenceNode<E, V> private constructor(
        private val sp: SharedPreferences,
        private val type: Type,
        private val key: String,
        private val default: V,
        getValue: ((E) -> V)? = null,
        fromValue: ((V) -> E)? = null
) {
    companion object {
        fun <E> forInt(sp: SharedPreferences,
                       key: String, default: Int,
                       toValue: ((E) -> Int)? = null,
                       fromValue: ((Int) -> E)? = null): PreferenceNode<E, Int> {
            return PreferenceNode(sp, Type.INT, key, default, toValue, fromValue)
        }

        fun <E> forLong(sp: SharedPreferences,
                       key: String, default: Long,
                       toValue: ((E) -> Long)? = null,
                       fromValue: ((Long) -> E)? = null): PreferenceNode<E, Long> {
            return PreferenceNode(sp, Type.LONG, key, default, toValue, fromValue)
        }

        fun <E> forString(sp: SharedPreferences,
                          key: String, default: String,
                          toValue: ((E) -> String)? = null,
                          fromValue: ((String) -> E)? = null): PreferenceNode<E, String> {
            return PreferenceNode(sp, Type.STRING, key, default, toValue, fromValue)
        }

        fun <E> forNullableString(sp: SharedPreferences,
                                  key: String, default: String?,
                                  toValue: ((E) -> String?)? = null,
                                  fromValue: ((String?) -> E)? = null): PreferenceNode<E, String?> {
            return PreferenceNode(sp, Type.STRING, key, default, toValue, fromValue)
        }

        fun <E> forBoolean(sp: SharedPreferences,
                           key: String, default: Boolean,
                           toValue: ((E) -> Boolean)? = null,
                           fromValue: ((Boolean) -> E)? = null): PreferenceNode<E, Boolean> {
            return PreferenceNode(sp, Type.BOOLEAN, key, default, toValue, fromValue)
        }
    }

    private val toValue: ((E) -> V) = getValue ?: { it as V }
    private val fromValue: ((V) -> E) = fromValue ?: { it as E }

    private val observable = KObservable(pull())

    val value: V get() = toValue(observable.value)
    val entity: E get() = observable.value

    init {
        if (type == Type.STRING && default != null && sp.getString(key, null) == null) {
            sp.edit().putString(key, default as String).apply()
        }
        // registerOnSharedPreferenceChangeListener() does not work :(
    }

    private fun pull(): E = fromValue(pullOriginal(sp))

    private fun pullOriginal(sp: SharedPreferences): V {
        return when (type) {
            Type.INT -> sp.getInt(key, default as Int) as V
            Type.LONG -> sp.getLong(key, default as Long) as V
            Type.STRING -> sp.getString(key, default as String?) as V
            Type.BOOLEAN -> sp.getBoolean(key, default as Boolean) as V
        }
    }

    fun push(entity: E) = pushByOriginal(toValue(entity))

    fun pushByOriginal(value: V) {
        when (type) {
            Type.INT -> sp.edit().putInt(key, value as Int).apply()
            Type.LONG -> sp.edit().putLong(key, value as Long).apply()
            Type.STRING -> sp.edit().putString(key, value as String?).apply()
            Type.BOOLEAN -> sp.edit().putBoolean(key, value as Boolean).apply()
        }
        observable.setAndNotify(fromValue(value))
    }

    fun notify(value: E) = observable.setAndNotify(value)

    fun notifyByOriginal(value: V) = observable.setAndNotify(fromValue(value))

    fun addObserver(removeCallback: KObservable.RemoveObserverCallback, observer: (E) -> Unit) {
        observable.addObserver(removeCallback, observer)
    }

    private enum class Type {
        INT, LONG, STRING, BOOLEAN
    }
}