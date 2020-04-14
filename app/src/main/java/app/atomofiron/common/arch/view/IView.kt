package app.atomofiron.common.arch.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.findBooleanByAttr
import ru.atomofiron.regextool.R

interface IView<P : BasePresenter<*,*>> : LifecycleOwner {
    companion object {
        const val UNDEFINED = -1
    }

    val systemBarsColorId: Int get() = UNDEFINED
    val systemBarsLights: Boolean get() = !thisContext.findBooleanByAttr(R.attr.isDarkTheme)

    val presenter: P

    val thisContext: Context
    val thisActivity: AppCompatActivity

    val mIntent: Intent

    fun isVisible(): Boolean

    fun inject()

    fun onCreate() = Unit

    fun onSubscribeData(owner: LifecycleOwner) = Unit
}