package app.atomofiron.common.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import kotlin.reflect.KClass

abstract class BaseActivity<M : BaseViewModel<*>> : AppCompatActivity() {

    protected abstract val viewModelClass: KClass<M>
    protected lateinit var viewModel: M

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(viewModelClass.java)
        viewModel.onActivityAttach(this)
        viewModel.onCreate(this, intent)
    }

    override fun onBackPressed() {
        viewModel.onBackButtonClick()
    }
}
