package app.atomofiron.common.arch

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

interface BaseFragment<F : Fragment, M : BaseViewModel<*,F,P>, P : BasePresenter<*,*>> {
    val viewModel: M
    val presenter: P get() = viewModel.presenter
    val isLightStatusBar: Boolean? get() = null

    fun initViewModel(fragment: F, viewModelClass: KClass<M>, state: Bundle?)
    fun onBack(): Boolean = false

    val Fragment.isTopVisible: Boolean get() = parentFragmentManager.fragments.findLast { it.isVisible } === this
}