package app.atomofiron.common.arch

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import app.atomofiron.common.util.KObservable
import app.atomofiron.common.util.property.MutableWeakProperty
import kotlinx.coroutines.CoroutineScope
import ru.atomofiron.regextool.App
import ru.atomofiron.regextool.log2

abstract class BaseViewModel<D, V : Any>(app: Application) : AndroidViewModel(app) {
    protected open lateinit var scope: CoroutineScope
    protected abstract val component: D

    protected val viewProperty = MutableWeakProperty<V>()
    val context: Context get() = getApplication<App>().applicationContext

    val onClearedCallback = KObservable.RemoveObserverCallback()

    open fun inject(view: V) {
        log2("inject")
        viewProperty.value = view
    }

    override fun onCleared() {
        super.onCleared()
        onClearedCallback.invoke()
    }
}
