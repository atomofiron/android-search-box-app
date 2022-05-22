package app.atomofiron.common.arch

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import app.atomofiron.common.util.property.MutableWeakProperty
import app.atomofiron.common.util.property.WeakProperty

abstract class BaseViewModel<D : Any, V : Fragment, P : BasePresenter<*,*>> : ViewModel() {
    private val fragmentProperty = MutableWeakProperty<Fragment>()

    abstract val presenter: P
    protected lateinit var component: D
        private set

    open fun inject(view: V) {
        fragmentProperty.value = view
        if (!::component.isInitialized) {
            component = createComponent(fragmentProperty)
        }
    }

    abstract fun createComponent(fragmentProperty: WeakProperty<Fragment>): D

    open fun onSaveState(state: Bundle) = Unit

    open fun onRestoreState(state: Bundle) = Unit

    override fun onCleared() {
        super.onCleared()
        presenter.onCleared()
    }
}