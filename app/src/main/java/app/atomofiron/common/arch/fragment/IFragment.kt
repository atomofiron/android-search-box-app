package app.atomofiron.common.arch.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.base.Backable
import app.atomofiron.common.util.findBooleanByAttr
import ru.atomofiron.regextool.R

interface IFragment<P : BasePresenter<*,*>> : Backable {

    val layoutId: Int
    val systemBarsColorId: Int
    val systemBarsLights: Boolean get() = !thisContext.findBooleanByAttr(R.attr.isDarkTheme)

    val presenter: P
    val delegate: FragmentDelegate<P>

    val thisContext: Context
    val thisActivity: AppCompatActivity
    val thisView: View

    val anchorView: View get() = thisActivity.findViewById(R.id.root_iv_joystick)

    fun buildComponentAndInject() = Unit

    fun onAttachChildFragment(childFragment: Fragment)

    fun onCreate() = Unit

    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View

    fun onStart()

    fun onResume()

    fun onPause()

    fun onSubscribeData(owner: LifecycleOwner) = Unit

    fun onHiddenChanged(hidden: Boolean)

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
}