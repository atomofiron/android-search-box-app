package app.atomofiron.searchboxapp.screens.main

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
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
import app.atomofiron.common.util.isDarkTheme
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.ActivityMainBinding
import app.atomofiron.searchboxapp.model.preference.AppOrientation
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.screens.main.fragment.SnackbarCallbackFragmentDelegate
import app.atomofiron.searchboxapp.screens.main.util.SnackbarWrapper
import app.atomofiron.searchboxapp.screens.main.util.offerKeyCodeToChildren
import app.atomofiron.searchboxapp.utils.Const
import lib.atomofiron.android_window_insets_compat.applyMarginInsets
import lib.atomofiron.android_window_insets_compat.insetsProxying
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
    private var theme: AppTheme? = null
    private val isDarkTheme: Boolean get() = isDarkTheme()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getAppTheme())
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.insetsProxying()
        binding.navHostFragment.insetsProxying()
        binding.joystick.applyMarginInsets(bottom = true)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.inject(this)
        presenter.onActivityCreate(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.joystick.setOnClickListener { onEscClick() }

        viewModel.showExitSnackbar.collect(lifecycleScope) {
            sbExit.show()
        }
        if (savedInstanceState == null) onIntent(intent)

        val manager = getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
        val onBackStackChangedListener: () -> Unit = {
            presenter.updateLightStatusBar(isDarkTheme)
            manager.hideSoftInputFromWindow(binding.root.windowToken, 0)
        }
        val childFragmentManager = supportFragmentManager.fragments.first().childFragmentManager
        childFragmentManager.addOnBackStackChangedListener(onBackStackChangedListener)
        supportFragmentManager.addOnBackStackChangedListener(onBackStackChangedListener)

        presenter.updateLightNavigationBar(isDarkTheme)
        presenter.updateLightStatusBar(isDarkTheme)
        setOrientation(viewModel.setOrientation.value)
        onCollect()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        onIntent(intent)
    }

    private fun onIntent(intent: Intent?) {
        presenter.onIntent(intent ?: return)
    }

    private fun getAppTheme(): AppTheme {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(Const.PREF_APP_THEME, null)
        val deepBlack = sp.getBoolean(Const.PREF_DEEP_BLACK, false)
        return AppTheme.fromString(name, deepBlack)
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
        when {
            theme == this.theme -> return
            theme is AppTheme.System && theme.deepBlack -> setTheme(R.style.AppTheme_Amoled)
            theme is AppTheme.System -> setTheme(R.style.AppTheme)
            theme is AppTheme.Light -> setTheme(R.style.AppTheme_Light)
            theme is AppTheme.Dark && theme.deepBlack -> setTheme(R.style.AppTheme_Black)
            theme is AppTheme.Dark -> setTheme(R.style.AppTheme_Dark)
        }
        window.decorView.setBackgroundColor(findColorByAttr(R.attr.colorBackground))
        this.theme = theme
        if (::presenter.isInitialized) presenter.applyTheme(isDarkTheme)
    }

    override fun onBackPressed() = presenter.onBackButtonClick()

    private fun onEscClick() {
        val viewWithFocus = binding.root.findFocus() as? EditText
        val consumed = viewWithFocus?.hideKeyboard() == true
        if (!consumed) {
            presenter.onEscClick()
        }
    }

    private fun setOrientation(orientation: AppOrientation) {
        if (requestedOrientation != orientation.constant) {
            requestedOrientation = orientation.constant
        }
    }
}