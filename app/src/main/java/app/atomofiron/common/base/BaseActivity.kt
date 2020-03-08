package app.atomofiron.common.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import app.atomofiron.common.util.findColorByAttr
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.iss.store.SettingsStore
import ru.atomofiron.regextool.model.AppTheme
import kotlin.reflect.KClass

abstract class BaseActivity<M : BaseViewModel<*>> : AppCompatActivity() {

    protected abstract val viewModelClass: KClass<M>
    protected lateinit var viewModel: M

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(SettingsStore.appTheme.entity)
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(viewModelClass.java)
        viewModel.onActivityAttach(this)
        viewModel.onCreate(this, intent)
        onSubscribeData(this)
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
}
