package app.atomofiron.common.arch

import android.content.Context
import androidx.lifecycle.ViewModel
import app.atomofiron.common.util.flow.LiveDataFlow
import app.atomofiron.common.util.property.MutableWeakProperty
import app.atomofiron.searchboxapp.App
import app.atomofiron.searchboxapp.logI

abstract class BaseViewModel<D, V : Any> : ViewModel() {
    protected abstract val component: D

    protected val viewProperty = MutableWeakProperty<V>()
    val context: Context get() = App.appContext//getApplication<App>().applicationContext

    val alerts = LiveDataFlow<String>(single = true)

    open fun inject(view: V) {
        logI("inject")
        viewProperty.value = view
    }

    override fun onCleared() {
        super.onCleared()
        logI("onCleared")
    }
}
