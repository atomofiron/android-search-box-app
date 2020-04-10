package app.atomofiron.common.arch.view

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BasePresenter
import ru.atomofiron.regextool.log2

class ViewDelegate<P : BasePresenter<*,*>> {
    private val systemBarsColorId: Int get() = view.systemBarsColorId
    private val systemBarsLights: Boolean get() = view.systemBarsLights
    private val layoutId: Int get() = view.layoutId

    private lateinit var view: IView<P>
    private val presenter: P get() = view.presenter

    private val thisContext: Context get() = view.thisContext
    private val thisActivity: AppCompatActivity get() = view.thisActivity

    private var isFirstTry = true; get() {
        val value = field
        field = false
        return value
    }

    init {
        log2("init")
    }

    fun onCreate(view: IView<P>) {
        this.view = view
        view.inject()
        presenter.onCreate(thisContext, view.intent)
        view.onCreate()
        view.onSubscribeData(view)
    }

    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        return LayoutInflater.from(inflater.context).inflate(layoutId, container, false)
    }

    fun onStart() {
        if (isFirstTry && view.isVisible() && systemBarsColorId != IView.UNDEFINED) {
            setStatusBarColor(systemBarsColorId)
            fixSystemBars(systemBarsLights)
        }
    }

    fun onAttachChildFragment(childFragment: Fragment) {
        presenter.onAttachChildFragment(childFragment)
    }

    fun onHiddenChanged(hidden: Boolean) {
        if (!hidden && systemBarsColorId != IView.UNDEFINED) {
            setStatusBarColor(systemBarsColorId)
            fixSystemBars(systemBarsLights)
        }
    }

    fun onBack(): Boolean = false

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
}