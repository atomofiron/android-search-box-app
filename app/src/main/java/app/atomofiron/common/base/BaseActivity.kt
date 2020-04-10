package app.atomofiron.common.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import app.atomofiron.common.util.findColorByAttr
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.model.AppTheme
import ru.atomofiron.regextool.utils.Const
import kotlin.reflect.KClass

abstract class BaseActivity<M : BaseViewModel<*>> : AppCompatActivity() {

    protected abstract val viewModelClass: KClass<M>
    protected lateinit var viewModel: M

    final override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getAppTheme())
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(viewModelClass.java)
        viewModel.onActivityAttach(this)
        viewModel.onCreate(this, intent)
        onCreate()
        onSubscribeData(this)
    }

    protected open fun onCreate() = Unit

    private fun getAppTheme(): AppTheme {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val themeIndex = sp.getString(Const.PREF_APP_THEME, null) ?: AppTheme.WHITE.ordinal.toString()
        return AppTheme.values()[themeIndex.toInt()]
    }

    protected open fun onSubscribeData(owner: LifecycleOwner) = Unit

    protected fun setTheme(theme: AppTheme) {
        when (theme) {
            AppTheme.WHITE -> setTheme(R.style.AppTheme_White)
            AppTheme.DARK -> setTheme(R.style.AppTheme_Dark)
            AppTheme.BLACK -> setTheme(R.style.AppTheme_Black)
        }
        window.decorView.setBackgroundColor(findColorByAttr(R.attr.colorBackground))
    }

    override fun onBackPressed() {
        viewModel.onBackButtonClick()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
    }
}
