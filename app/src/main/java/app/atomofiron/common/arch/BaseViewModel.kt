package app.atomofiron.common.arch

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import app.atomofiron.common.util.property.MutableWeakProperty

abstract class BaseViewModel<D : Any, V : Fragment, S : Any, P : BasePresenter<*,*>> : ViewModel() {

    protected val fragmentProperty = MutableWeakProperty<Fragment>()

    abstract val presenter: P
    abstract val viewState: S
    private lateinit var componentRef: D

    open fun setFragment(fragment: V) {
        fragmentProperty.value = fragment
        if (!::componentRef.isInitialized) {
            componentRef = component(fragment)
        }
    }

    abstract fun component(fragment: V): D

    open fun onSaveState(state: Bundle) = Unit

    open fun onRestoreState(state: Bundle) = Unit

    override fun onCleared() {
        super.onCleared()
        presenter.onCleared()
    }
}