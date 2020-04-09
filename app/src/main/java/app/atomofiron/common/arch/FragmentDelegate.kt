package app.atomofiron.common.arch

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.fragment.BaseFragment
import app.atomofiron.common.arch.fragment.IFragment
import app.atomofiron.common.util.findBooleanByAttr
import app.atomofiron.common.util.hideKeyboard
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.log2

class FragmentDelegate<P : BasePresenter<*,*>> : IFragment<P> {
    companion object {
        private const val LATEINIT_INT = 0
    }

    override val systemBarsColorId: Int = R.color.transparent
    override val systemBarsLights: Boolean get() = !thisContext.findBooleanByAttr(R.attr.isDarkTheme)

    override var layoutId: Int = LATEINIT_INT

    lateinit var fragment: BaseFragment<*,P>
    override lateinit var presenter: P

    override val delegate: FragmentDelegate<P> = this
    override val thisContext: Context get() = fragment.requireContext()
    override val thisActivity: AppCompatActivity get() = fragment.requireActivity() as AppCompatActivity
    override val thisView: View get() = fragment.requireView()

    private val visibilityWatcher = VisibilityWatcher()

    init {
        log2("init")
    }

    override fun onCreate() {
        // Fragment.onAttachFragment()
        fragment.buildComponentAndInject()
        layoutId = fragment.layoutId
        presenter = fragment.presenter
        presenter.onCreate(thisContext, fragment.arguments)
        fragment.onCreate()
        fragment.onSubscribeData(fragment)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return LayoutInflater.from(inflater.context).inflate(layoutId, container, false)
    }

    override fun onBack(): Boolean {
        val viewWithFocus = thisView.findFocus()
        // todo это вообще работает как задумано?
        return viewWithFocus?.hideKeyboard() != null
    }

    override fun onStart() {
        // todo bad idea do this in onStart()
        setStatusBarColor(systemBarsColorId)
        fixSystemBars(systemBarsLights)
    }

    override fun onResume() {
        visibilityWatcher.resumed = true
    }

    override fun onPause() {
        visibilityWatcher.resumed = false
    }

    override fun onAttachChildFragment(childFragment: Fragment) {
        presenter.onAttachChildFragment(childFragment)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            setStatusBarColor(systemBarsColorId)
            fixSystemBars(systemBarsLights)
        }
        visibilityWatcher.hidden = hidden
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setStatusBarColor(colorId: Int) {
        val color = ContextCompat.getColor(thisContext, colorId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            thisActivity.window.statusBarColor = color
            thisActivity.window.navigationBarColor = color
        }
    }

    private fun fixSystemBars(windowLightBars: Boolean) {
        // fix of the bug, when the flag is not applied by the system
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            thisActivity.window.decorView.apply {
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

    private inner class VisibilityWatcher {
        private val visible: Boolean get() = !hidden && resumed

        var hidden = false
            set(value) {
                if (field == value) return
                val state = visible
                field = value
                if (state != visible) presenter.onVisibleChanged(visible)
            }
        var resumed = false
            set(value) {
                if (field == value) return
                val state = visible
                field = value
                if (state != visible) presenter.onVisibleChanged(visible)
            }
    }
}