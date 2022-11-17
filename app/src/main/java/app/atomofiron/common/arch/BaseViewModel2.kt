package app.atomofiron.common.arch

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import app.atomofiron.common.util.property.MutableWeakProperty

abstract class BaseViewModel2<D : Any, V : Fragment, S : Any, P : BasePresenter<*,*>> : ViewModel() {

    protected val fragmentProperty = MutableWeakProperty<Fragment>()

    abstract val presenter: P
    abstract val viewState: S
    abstract val component: D

    open fun setFragment(view: V) {
        fragmentProperty.value = view
    }

    open fun onSaveState(state: Bundle) = Unit

    open fun onRestoreState(state: Bundle) = Unit

    override fun onCleared() {
        super.onCleared()
        presenter.onCleared()
    }
}