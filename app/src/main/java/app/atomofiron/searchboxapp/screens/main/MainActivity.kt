package app.atomofiron.searchboxapp.screens.main

import android.os.Bundle
import android.view.KeyEvent
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.value
import app.atomofiron.common.util.hideKeyboard
import app.atomofiron.common.util.insets.ViewGroupInsetsProxy
import app.atomofiron.common.util.insets.ViewInsetsController
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.ActivityMainBinding
import app.atomofiron.searchboxapp.model.preference.AppOrientation
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.screens.main.fragment.SnackbarCallbackFragmentDelegate
import app.atomofiron.searchboxapp.screens.main.util.SnackbarWrapper
import app.atomofiron.searchboxapp.screens.main.util.offerKeyCodeToChildren
import app.atomofiron.searchboxapp.utils.Const
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val rooFragment: Fragment get() = binding.navHostFragment.getFragment()

    private val sbExit: SnackbarWrapper = SnackbarWrapper(this) {
        Snackbar.make(binding.root, R.string.click_back_to_exit, Snackbar.LENGTH_SHORT)
            .setAnchorView(binding.joystick)
            .setActionTextColor(this@MainActivity.findColorByAttr(R.attr.colorAccent))
            .setAction(R.string.exit) { presenter.onExitClick() }
            .addCallback(SnackbarCallbackFragmentDelegate(presenter))
    }

    private lateinit var viewModel: MainViewModel
    @Inject
    lateinit var presenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getAppTheme())
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewGroupInsetsProxy.set(binding.root)
        ViewGroupInsetsProxy.set(binding.navHostFragment)
        ViewInsetsController.bindMargin(binding.joystick, bottom = true)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.inject(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.joystick.setOnClickListener { onEscClick() }

        viewModel.showExitSnackbar.collect(lifecycleScope) {
            sbExit.show()
        }

        setOrientation(viewModel.setOrientation.value)
        onCollect()
    }

    private fun getAppTheme(): AppTheme {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val themeIndex = sp.getString(Const.PREF_APP_THEME, null) ?: AppTheme.WHITE.ordinal.toString()
        return AppTheme.values()[themeIndex.toInt()]
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when  {
            super.onKeyDown(keyCode, event) -> Unit
            keyCode == KeyEvent.KEYCODE_ESCAPE -> onEscClick()
            rooFragment.offerKeyCodeToChildren(keyCode) -> Unit
            else -> return false
        }
        return true
    }

    override fun setTheme(resId: Int) {
        super.setTheme(resId)
        if (::binding.isInitialized) {
            binding.root.setBackgroundColor(findColorByAttr(R.attr.colorBackground))
            binding.joystick.setComposition()
        }
    }

    private fun onCollect() = viewModel.apply {
        setTheme.collect(lifecycleScope, ::setTheme)
        setOrientation.collect(lifecycleScope, ::setOrientation)
        setJoystick.collect(lifecycleScope) {
            binding.joystick.setComposition(it)
        }
    }

    private fun setTheme(theme: AppTheme) {
        when (theme) {
            AppTheme.WHITE -> setTheme(R.style.AppTheme_White)
            AppTheme.DARK -> setTheme(R.style.AppTheme_Dark)
            AppTheme.BLACK -> setTheme(R.style.AppTheme_Black)
        }
        window.decorView.setBackgroundColor(findColorByAttr(R.attr.colorBackground))
    }

    override fun onBackPressed() = presenter.onBackButtonClick()

    private fun onEscClick() {
        val viewWithFocus = binding.root.findFocus() as? EditText
        val consumed = viewWithFocus?.hideKeyboard() != null
        if (!consumed) {
            presenter.onJoystickClick()
        }
    }

    private fun setOrientation(orientation: AppOrientation) {
        if (requestedOrientation != orientation.constant) {
            requestedOrientation = orientation.constant
        }
    }
}