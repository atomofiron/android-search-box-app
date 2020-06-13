package app.atomofiron.common.arch.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceFragmentCompat
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.arch.view.Backable
import app.atomofiron.common.arch.view.IView
import app.atomofiron.common.arch.view.ViewDelegate
import app.atomofiron.common.util.findBooleanByAttr
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.logI
import kotlin.reflect.KClass

abstract class BasePreferenceFragment<M : BaseViewModel<*,*>, P : BasePresenter<*,*>> : PreferenceFragmentCompat(), IView<P>, Backable {

    override val systemBarsColorId: Int get() = R.color.transparent
    override val systemBarsLights: Boolean get() = !thisContext.findBooleanByAttr(R.attr.isDarkTheme)

    protected abstract val viewModelClass: KClass<M>
    protected lateinit var viewModel: M

    override val thisContext: Context get() = requireContext()
    override val thisActivity: AppCompatActivity get() = requireActivity() as AppCompatActivity
    val thisView: View get() = requireView()
    protected val anchorView: View get() = thisActivity.findViewById(R.id.root_iv_joystick)
    override val lifecycleOwner: LifecycleOwner get() = this // still not viewLifecycleOwner

    private val delegate = ViewDelegate<P>()
    override fun getIntent(): Intent = Intent().putExtras(arguments ?: Bundle())

    init {
        logI("init")
    }

    final override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        logI("onCreate")
        viewModel = ViewModelProvider(this).get(viewModelClass.java)
        delegate.onCreate(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        delegate.onViewCreated()
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

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        delegate.onHiddenChanged(hidden)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        delegate.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}