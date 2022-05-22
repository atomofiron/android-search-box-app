package app.atomofiron.common.arch

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlin.reflect.KClass

class BaseFragmentImpl<F : Fragment, M : BaseViewModel<*,F,P>, P : BasePresenter<*,*>> : BaseFragment<F,M,P> {

    override lateinit var viewModel: M

    override fun initViewModel(fragment: F, viewModelClass: KClass<M>, state: Bundle?) {
        viewModel = ViewModelProvider(fragment)[viewModelClass.java]
        viewModel.inject(fragment)
        if (state != null) viewModel.onRestoreState(state)
    }
}