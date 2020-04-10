package app.atomofiron.common.arch.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.findBooleanByAttr
import ru.atomofiron.regextool.R

interface IView<P : BasePresenter<*,*>> : LifecycleOwner, Backable {
    companion object {
        const val UNDEFINED = -1
    }

    val layoutId: Int
    val systemBarsColorId: Int get() = UNDEFINED
    val systemBarsLights: Boolean get() = !thisContext.findBooleanByAttr(R.attr.isDarkTheme)

    val presenter: P

    val thisContext: Context
    val thisActivity: AppCompatActivity

    val anchorView: View get() = thisActivity.findViewById(R.id.root_iv_joystick)
    val intent: Intent
    fun isVisible(): Boolean

    fun inject() = Unit

    fun onCreate() = Unit

    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View

    fun onStart()

    fun onSubscribeData(owner: LifecycleOwner) = Unit

    fun onHiddenChanged(hidden: Boolean)

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
}