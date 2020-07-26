package app.atomofiron.common.arch.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStore
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.findBooleanByAttr
import app.atomofiron.searchboxapp.R

interface IView<P : BasePresenter<*,*>> : LifecycleOwner {
    companion object {
        const val UNDEFINED = -1
    }

    val systemBarsColorId: Int get() = UNDEFINED
    val systemBarsLights: Boolean get() = !thisContext.findBooleanByAttr(R.attr.isDarkTheme)

    val presenter: P

    val thisContext: Context
    val thisActivity: AppCompatActivity
    val lifecycleOwner: LifecycleOwner

    fun getIntent(): Intent

    fun getViewModelStore(): ViewModelStore

    fun isVisible(): Boolean

    fun inject()

    fun onCreate() = Unit

    fun onSubscribeData(owner: LifecycleOwner) = Unit
}