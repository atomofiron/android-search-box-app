package ru.atomofiron.regextool.iss.store

import ru.atomofiron.regextool.App
import ru.atomofiron.regextool.common.util.KObservable
import ru.atomofiron.regextool.utils.sp

class PreferenceStore<E, V> private constructor(
        private val type: Type,
        private val key: String,
        private val default: V,
        getValue: ((E) -> V)? = null,
        fromValue: ((V) -> E)? = null
) {
    companion object {
        private val sp by lazy { App.context.sp() }

        fun <E> forInt(key: String, default: Int,
                       toValue: ((E) -> Int)? = null,
                       fromValue: ((Int) -> E)? = null): PreferenceStore<E, Int> {
            return PreferenceStore(Type.INT, key, default, toValue, fromValue)
        }

        fun <E> forString(key: String, default: String,
                          toValue: ((E) -> String)? = null,
                          fromValue: ((String) -> E)? = null): PreferenceStore<E, String> {
            return PreferenceStore(Type.STRING, key, default, toValue, fromValue)
        }

        fun <E> forNullableString(key: String, default: String?,
                          toValue: ((E) -> String?)? = null,
                          fromValue: ((String?) -> E)? = null): PreferenceStore<E, String?> {
            return PreferenceStore(Type.STRING, key, default, toValue, fromValue)
        }

        fun <E> forBoolean(key: String, default: Boolean,
                           toValue: ((E) -> Boolean)? = null,
                           fromValue: ((Boolean) -> E)? = null): PreferenceStore<E, Boolean> {
            return PreferenceStore(Type.BOOLEAN, key, default, toValue, fromValue)
        }
    }

    private val toValue: ((E) -> V) = getValue ?: { it as V }
    private val fromValue: ((V) -> E) = fromValue ?: { it as E }

    private val observable = KObservable(pull())

    val value: E get() = observable.value

    private fun pull(): E {
        return when (type) {
            Type.INT -> fromValue(sp.getInt(key, default as Int) as V)
            Type.STRING -> fromValue(sp.getString(key, default as String?) as V)
            Type.BOOLEAN -> fromValue(sp.getBoolean(key, default as Boolean) as V)
        }
    }

    fun push(value: E) {
        observable.setAndNotify(value)
        when (type) {
            Type.INT -> sp.edit().putInt(key, toValue(value) as Int).apply()
            Type.STRING -> sp.edit().putString(key, toValue(value) as String?).apply()
            Type.BOOLEAN -> sp.edit().putBoolean(key, toValue(value) as Boolean).apply()
        }
    }

    fun notify(value: E) = observable.setAndNotify(value)

    fun notifyByOriginal(value: V) = observable.setAndNotify(fromValue(value))

    fun addObserver(removeCallback: KObservable.RemoveObserverCallback, observer: (E) -> Unit) {
        observable.addObserver(removeCallback, observer)
    }

    private enum class Type {
        INT, STRING, BOOLEAN
    }
}