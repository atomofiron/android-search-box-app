package app.atomofiron.searchboxapp.injectable.store.util

import android.content.SharedPreferences
import app.atomofiron.common.util.flow.DataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

        fun <E> forBoolean(sp: SharedPreferences,
                           key: String, default: Boolean,
                           toValue: ((E) -> Boolean)? = null,
                           fromValue: ((Boolean) -> E)? = null): PreferenceNode<E, Boolean> {
            return PreferenceNode(sp, Type.BOOLEAN, key, default, toValue, fromValue)
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

        fun <E> forSet(sp: SharedPreferences, key: String, default: Set<String>,
                       toValue: ((E) -> Set<String>)? = null,
                       fromValue: ((Set<String>) -> E)? = null): PreferenceNode<E, Set<String>> {
            return PreferenceNode(sp, Type.SET, key, default, toValue, fromValue)
        }
    }

    private val toValue: ((E) -> V) = getValue ?: { it as V }
    private val fromValue: ((V) -> E) = fromValue ?: { it as E }

    private val flow = DataFlow(pullEntity())

    val value: V get() = toValue(flow.value)
    val entity: E get() = flow.value

    init {
        val default = default as? String
        if (type == Type.STRING && default != null && sp.getString(key, null) == null) {
            sp.edit().putString(key, default).apply()
            flow.value = fromValue(default as V)
        }
        // registerOnSharedPreferenceChangeListener() does not work :(
    }

    private fun pullEntity(): E = fromValue(pullOriginal())

    private fun pullOriginal(): V {
        return when (type) {
            Type.INT -> sp.getInt(key, default as Int) as V
            Type.LONG -> sp.getLong(key, default as Long) as V
            Type.BOOLEAN -> sp.getBoolean(key, default as Boolean) as V
            Type.STRING -> sp.getString(key, default as String?) as V
            Type.SET -> sp.getStringSet(key, default as Set<String>?) as V
        }
    }

    fun pushByEntity(entity: E) = pushByOriginal(toValue(entity))

    fun pushByOriginal(value: V) {
        when (type) {
            Type.INT -> sp.edit().putInt(key, value as Int).apply()
            Type.LONG -> sp.edit().putLong(key, value as Long).apply()
            Type.BOOLEAN -> sp.edit().putBoolean(key, value as Boolean).apply()
            Type.STRING -> sp.edit().putString(key, value as String?).apply()
            Type.SET -> sp.edit().putStringSet(key, value as Set<String>?).apply()
        }
        flow.value = fromValue(value)
    }

    fun notify(value: E) = flow.emit(value)

    fun notifyByOriginal(value: V) = flow.emit(fromValue(value))

    fun collect(scope: CoroutineScope, collector: suspend (E) -> Unit) {
        scope.launch {
            flow.collect(collector)
        }
    }

    private enum class Type {
        INT, LONG, BOOLEAN, STRING, SET
    }
}