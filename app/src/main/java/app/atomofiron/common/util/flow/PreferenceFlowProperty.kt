package app.atomofiron.common.util.flow

import android.content.SharedPreferences
import app.atomofiron.common.util.PreferenceKey
import app.atomofiron.common.util.get
import app.atomofiron.common.util.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

class PreferenceFlowProperty<T, N : T?>(
    private val preferences: SharedPreferences,
    private val key: PreferenceKey<T>,
    flow: MutableStateFlow<N>,
) : MutableStateFlow<N> by flow {
    companion object {
        fun <T, N : T?> create(
            scope: CoroutineScope,
            updates: Flow<String>,
            preferences: SharedPreferences,
            key: PreferenceKey<T>,
            default: N,
        ): PreferenceFlowProperty<T, N> {
            val initial = preferences.get(key, default) as N
            val flow = MutableStateFlow(initial)
            scope.launch(Dispatchers.Main.immediate) {
                updates.collect {
                    if (it == key.name) {
                        flow.value = preferences.get(key, default) as N
                    }
                }
            }
            return PreferenceFlowProperty(preferences, key, flow)
        }
    }

    operator fun getValue(any: Any, property: KProperty<*>): N = value

    operator fun setValue(any: Any, property: KProperty<*>, value: N) {
        if (value == this.value) return
        this.value = value
        preferences.put(key, value)
    }
}