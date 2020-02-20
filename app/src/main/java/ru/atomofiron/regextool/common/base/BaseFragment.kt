package ru.atomofiron.regextool.common.base

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.util.findBooleanByAttr
import ru.atomofiron.regextool.log2
import kotlin.reflect.KClass

abstract class BaseFragment<M : BaseViewModel<*>> : Fragment() {
    protected abstract val viewModelClass: KClass<M>
    protected lateinit var viewModel: M
    protected val dataProvider: M get() = viewModel

    protected abstract val layoutId: Int
    protected open val systemBarsColorId: Int = R.color.transparent
    protected open val systemBarsLights: Boolean get() = !context.findBooleanByAttr(R.attr.isDarkTheme)

    init {
        log2("init")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(activity!!).get(viewModelClass.java)
        viewModel.onFragmentAttach(this)
        viewModel.onCreate(context!!, arguments)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? = LayoutInflater.from(context).inflate(layoutId, container, false)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onShow()
        onSubscribeData(this)
    }

    open fun onBack(): Boolean = false

    override fun onStart() {
        super.onStart()

        // todo bad idea do this in onStart()
        setStatusBarColor(systemBarsColorId)
        fixSystemBars(systemBarsLights)
    }

    open fun onSubscribeData(owner: LifecycleOwner) = Unit

    open fun onUnsubscribeData(owner: LifecycleOwner) = Unit

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (!hidden) {
            viewModel.onShow()
            setStatusBarColor(systemBarsColorId)
            fixSystemBars(systemBarsLights)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        viewModel.onViewDestroy()
        super.onDestroy()
    }

    private fun setStatusBarColor(colorId: Int) {
        val activity = activity as AppCompatActivity
        val color = ContextCompat.getColor(activity, colorId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.statusBarColor = color
            activity.window.navigationBarColor = color
        }
    }

    private fun fixSystemBars(windowLightBars: Boolean) {
        // fix of the bug, when the flag is not applied by the system
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity!!.window.decorView.apply {
                systemUiVisibility = when {
                    windowLightBars -> systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    else -> systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    systemUiVisibility = when {
                        windowLightBars -> systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                        else -> systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                    }
                }
            }
        }
    }
}