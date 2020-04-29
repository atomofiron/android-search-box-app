package app.atomofiron.common.arch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import app.atomofiron.common.arch.view.IView
import app.atomofiron.common.arch.view.ViewDelegate
import app.atomofiron.common.util.findColorByAttr
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.model.preference.AppTheme
import ru.atomofiron.regextool.utils.Const
import kotlin.reflect.KClass

abstract class BaseActivity<M : BaseViewModel<*,*>, P : BasePresenter<*,*>> : AppCompatActivity(), IView<P> {

    protected abstract val viewModelClass: KClass<M>
    protected lateinit var viewModel: M
    private val delegate = ViewDelegate<P>()

    override val thisContext: Context get() = this
    override val thisActivity: AppCompatActivity get() = this
    override val lifecycleOwner: LifecycleOwner get() = this

    final override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getAppTheme())
        super<AppCompatActivity>.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(viewModelClass.java)
        delegate.onCreate(this)
    }

    override fun isVisible(): Boolean = lifecycle.currentState == Lifecycle.State.STARTED

    override fun onStart() {
        super.onStart()
        delegate.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        delegate.onDestroy()
    }

    private fun getAppTheme(): AppTheme {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val themeIndex = sp.getString(Const.PREF_APP_THEME, null) ?: AppTheme.WHITE.ordinal.toString()
        return AppTheme.values()[themeIndex.toInt()]
    }

    protected fun setTheme(theme: AppTheme) {
        when (theme) {
            AppTheme.WHITE -> setTheme(R.style.AppTheme_White)
            AppTheme.DARK -> setTheme(R.style.AppTheme_Dark)
            AppTheme.BLACK -> setTheme(R.style.AppTheme_Black)
        }
        window.decorView.setBackgroundColor(findColorByAttr(R.attr.colorBackground))
    }
}
