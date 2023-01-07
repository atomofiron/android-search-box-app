package app.atomofiron.searchboxapp.screens.main

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.atomofiron.common.util.findBooleanByAttr
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.hideKeyboard
import app.atomofiron.common.util.isDarkTheme
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.OrientationLayoutDelegate.Companion.syncOrientation
import app.atomofiron.searchboxapp.databinding.ActivityMainBinding
import app.atomofiron.searchboxapp.model.preference.AppOrientation
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.screens.main.fragment.SnackbarCallbackFragmentDelegate
import app.atomofiron.searchboxapp.screens.main.util.SnackbarWrapper
import app.atomofiron.searchboxapp.screens.main.util.offerKeyCodeToChildren
import lib.atomofiron.android_window_insets_compat.applyMarginInsets
import lib.atomofiron.android_window_insets_compat.insetsProxying

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val rooFragment: Fragment get() = binding.navHostFragment.getFragment()

    private val sbExit: SnackbarWrapper = SnackbarWrapper(this) {
        Snackbar.make(binding.root, R.string.click_back_to_exit, Snackbar.LENGTH_SHORT)
            .setAnchorView(binding.root.findViewById(R.id.bottom_bar))
            .setAction(R.string.exit) { presenter.onExitClick() }
            .addCallback(SnackbarCallbackFragmentDelegate(presenter))
    }

    private lateinit var viewState: MainViewState
    private lateinit var presenter: MainPresenter
    private var isFirstStart = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.setView(this)
        presenter = viewModel.presenter
        viewState = viewModel.viewState
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = presenter.onBackButtonClick()
        })

        updateTheme(viewState.setTheme.value)
        onCreateView(savedInstanceState)

        stringFromJNI(100)
    }

    private fun onCreateView(savedInstanceState: Bundle?) {
        // todo Caused by: java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyInsets()

        presenter.onActivityCreate(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.joystick.setOnClickListener { onEscClick() }
        binding.joystick.syncOrientation(binding.root)

        viewState.showExitSnackbar.collect(lifecycleScope) {
            sbExit.show()
        }
        if (savedInstanceState == null) onIntent(intent)

        val manager = getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
        val onBackStackChangedListener: () -> Unit = {
            presenter.updateLightStatusBar(isDarkTheme())
            manager.hideSoftInputFromWindow(binding.root.windowToken, 0)
        }
        val childFragmentManager = supportFragmentManager.fragments.first().childFragmentManager
        childFragmentManager.addOnBackStackChangedListener(onBackStackChangedListener)
        supportFragmentManager.addOnBackStackChangedListener(onBackStackChangedListener)

        presenter.updateLightNavigationBar(isDarkTheme())
        presenter.updateLightStatusBar(isDarkTheme())
        onCollect()
    }

    override fun onStart() {
        super.onStart()
        when {
            isFirstStart -> isFirstStart = false
            else -> presenter.onMaximize()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onActivityDestroy()
    }

    private fun applyInsets() {
        binding.root.insetsProxying()
        binding.navHostFragment.insetsProxying()
        binding.joystick.applyMarginInsets()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        onIntent(intent)
    }

    private fun onIntent(intent: Intent?) {
        presenter.onIntent(intent ?: return)
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

    private fun onCollect() {
        viewState.apply {
            setTheme.collect(lifecycleScope, ::updateTheme)
            setOrientation.collect(lifecycleScope, ::setOrientation)
            setJoystick.collect(lifecycleScope, binding.joystick::setComposition)
        }
    }

    private fun updateTheme(theme: AppTheme) {
        when {
            theme is AppTheme.Light -> Unit
            theme.deepBlack == findBooleanByAttr(R.attr.isBlackDeep) -> Unit
            theme.deepBlack -> setTheme(R.style.CompatTheme_Amoled)
            else -> setTheme(R.style.CompatTheme)
        }
        presenter.onThemeApplied(isDarkTheme())
    }

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

    external fun stringFromJNI(dur: Int): String

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("toybox")
            System.loadLibrary("native-lib")
        }
    }
}