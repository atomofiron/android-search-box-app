package app.atomofiron.common.arch

import android.content.Context
import androidx.lifecycle.ViewModel
import app.atomofiron.common.util.KObservable
import app.atomofiron.common.util.SingleLiveEvent
import app.atomofiron.common.util.property.MutableWeakProperty
import ru.atomofiron.regextool.App
import ru.atomofiron.regextool.logI

abstract class BaseViewModel<D, V : Any> : ViewModel() {
    protected abstract val component: D

    protected val viewProperty = MutableWeakProperty<V>()
    val context: Context get() = App.appContext//getApplication<App>().applicationContext

    // будет заменён корутинами
    val onClearedCallback = KObservable.RemoveObserverCallback()
    val alerts = SingleLiveEvent<String>()

    lateinit var onClearedListener: () -> Unit

    open fun inject(view: V) {
        logI("inject")
        viewProperty.value = view
    }

    override fun onCleared() {
        super.onCleared()
        onClearedCallback.invoke()
        onClearedListener.invoke()
    }
}
