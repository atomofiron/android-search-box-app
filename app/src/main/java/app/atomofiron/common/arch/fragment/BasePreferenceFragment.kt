package app.atomofiron.common.arch.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceFragmentCompat
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.arch.view.Backable
import app.atomofiron.common.arch.view.IView
import app.atomofiron.common.arch.view.ViewDelegate
import app.atomofiron.common.util.findBooleanByAttr
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.log2
import kotlin.reflect.KClass

abstract class BasePreferenceFragment<M : BaseViewModel<*,*>, P : BasePresenter<*,*>> : PreferenceFragmentCompat(), IView<P>, Backable {

    override val systemBarsColorId: Int get() = R.color.transparent
    override val systemBarsLights: Boolean get() = !thisContext.findBooleanByAttr(R.attr.isDarkTheme)

    protected abstract val layoutId: Int
    protected abstract val viewModelClass: KClass<M>

    protected lateinit var viewModel: M
    abstract override val presenter: P

    override val thisContext: Context get() = requireContext()
    override val thisActivity: AppCompatActivity get() = requireActivity() as AppCompatActivity
    val thisView: View get() = requireView()
    protected val anchorView: View get() = thisActivity.findViewById(R.id.root_iv_joystick)

    private val delegate = ViewDelegate<P>()
    override val intent: Intent get() = Intent().putExtras(arguments ?: Bundle())

    init {
        log2("init")
    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        super<PreferenceFragmentCompat>.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(viewModelClass.java)
        delegate.onCreate(this)
    }

    override fun onStart() {
        super.onStart()
        delegate.onStart()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        delegate.onHiddenChanged(hidden)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        delegate.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}