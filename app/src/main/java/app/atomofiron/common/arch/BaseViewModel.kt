package app.atomofiron.common.arch

import android.content.Context
import androidx.lifecycle.ViewModel
import app.atomofiron.common.util.KObservable
import app.atomofiron.common.util.SingleLiveEvent
import app.atomofiron.common.util.property.MutableWeakProperty
import ru.atomofiron.regextool.App
import ru.atomofiron.regextool.log2

abstract class BaseViewModel<D, V : Any> : ViewModel() {
    protected abstract val component: D

    protected val viewProperty = MutableWeakProperty<V>()
    val context: Context get() = App.appContext//getApplication<App>().applicationContext

    val onClearedCallback = KObservable.RemoveObserverCallback()
    val alerts = SingleLiveEvent<String>()

    open fun inject(view: V) {
        log2("inject")
        viewProperty.value = view
    }

    override fun onCleared() {
        super.onCleared()
        log2("onCleared")
        onClearedCallback.invoke()
    }
}
