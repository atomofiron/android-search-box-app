package app.atomofiron.common.arch.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.arch.FragmentDelegate
import app.atomofiron.common.base.Backable
import ru.atomofiron.regextool.log2

abstract class BaseFragment<M : BaseViewModel, P : BasePresenter<*,*>> : Fragment(), IFragment<P> by FragmentDelegate<P>(), Backable {

    abstract override val layoutId: Int
    abstract val viewModel: M

    init {
        log2("init")
    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate.fragment = this
        delegate.onCreate()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return delegate.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onBack(): Boolean {
        // todo try remove
        return delegate.onBack()
    }

    override fun onStart() {
        super.onStart()
        delegate.onStart()
    }

    override fun onResume() {
        super.onResume()
        delegate.onResume()
    }

    override fun onPause() {
        super.onPause()
        delegate.onPause()
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        delegate.onAttachChildFragment(childFragment)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        delegate.onHiddenChanged(hidden)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        delegate.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}