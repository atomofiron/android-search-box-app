package app.atomofiron.common.arch.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.findBooleanByAttr
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.log2

abstract class BaseFragment<M : BaseViewModel, P : BasePresenter<*,*>> : Fragment(), IFragment<P> by FragmentDelegate<P>() {
    override val systemBarsColorId: Int get() = R.color.transparent
    override val systemBarsLights: Boolean get() = thisContext.findBooleanByAttr(R.attr.isDarkTheme)
    abstract override val layoutId: Int

    abstract val viewModel: M

    init {
        log2("init")
    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        super<Fragment>.onCreate(savedInstanceState)
        delegate.onCreate(this)
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