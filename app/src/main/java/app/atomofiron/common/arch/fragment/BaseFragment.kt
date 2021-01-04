package app.atomofiron.common.arch.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.arch.view.Backable
import app.atomofiron.common.arch.view.IView
import app.atomofiron.common.arch.view.ViewDelegate
import app.atomofiron.common.util.findBooleanByAttr
import app.atomofiron.common.util.flow.viewCollect
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.logI
import kotlin.reflect.KClass

abstract class BaseFragment<M : BaseViewModel<*,*>, P : BasePresenter<*,*>> : Fragment(), IView<P>, Backable {

    override val systemBarsColorId: Int get() = R.color.transparent
    override val systemBarsLights: Boolean get() = !thisContext.findBooleanByAttr(R.attr.isDarkTheme)

    protected abstract val layoutId: Int
    protected abstract val viewModelClass: KClass<M>
    protected lateinit var viewModel: M

    override val thisContext: Context get() = requireContext()
    override val thisActivity: AppCompatActivity get() = requireActivity() as AppCompatActivity
    val thisView: View get() = requireView()
    val thisArguments: Bundle get() = requireArguments()
    protected val anchorView: View get() = thisActivity.findViewById(R.id.root_iv_joystick)
    override val lifecycleOwner: LifecycleOwner get() = this // still not viewLifecycleOwner

    private val delegate = ViewDelegate<P>()
    override fun getIntent(): Intent = Intent().putExtras(arguments ?: Bundle())

    init {
        logI("init")
    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        super<Fragment>.onCreate(savedInstanceState)
        logI("onCreate")
        // onAttachChildFragment()
        viewModel = ViewModelProvider(this).get(viewModelClass.java)
        delegate.onCreate(this)
    }

    protected open fun onAlert(message: String) {
        Snackbar.make(thisView, message, Snackbar.LENGTH_SHORT)
                .setAnchorView(anchorView)
                .show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return LayoutInflater.from(inflater.context).inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        delegate.onViewCreated()
        viewCollect(viewModel.alerts, ::onAlert)
    }

    override fun onStart() {
        super.onStart()
        delegate.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        logI("onDestroy $isRemoving")
        delegate.onDestroy()
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