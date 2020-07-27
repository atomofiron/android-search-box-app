package app.atomofiron.searchboxapp.screens.root

import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import app.atomofiron.common.arch.BaseActivity
import app.atomofiron.common.util.Knife
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.Joystick
import app.atomofiron.searchboxapp.model.preference.AppOrientation
import app.atomofiron.searchboxapp.screens.explorer.ExplorerFragment
import app.atomofiron.searchboxapp.screens.root.fragment.SnackbarCallbackFragmentDelegate
import app.atomofiron.searchboxapp.screens.root.util.SnackbarWrapper
import javax.inject.Inject
import kotlin.reflect.KClass

open class RootActivity : BaseActivity<RootViewModel, RootPresenter>() {

    override val viewModelClass: KClass<RootViewModel> = RootViewModel::class

    @Inject
    override lateinit var presenter: RootPresenter

    private val root = Knife<ConstraintLayout>(this, R.id.root_cl_root)
    private val joystick = Knife<Joystick>(this, R.id.root_iv_joystick)

    private lateinit var explorerFragment: ExplorerFragment

    private val sbExitSnackbarContextView: View get() = findViewById(R.id.finder_bom) ?: joystick.view
    private val sbExit: SnackbarWrapper = SnackbarWrapper(this) {
        Snackbar.make(sbExitSnackbarContextView, R.string.click_back_to_exit, Snackbar.LENGTH_SHORT)
                .setAnchorView(joystick.view)
                .setActionTextColor(this@RootActivity.findColorByAttr(R.attr.colorAccent))
                .setAction(R.string.exit) { presenter.onExitClick() }
                .addCallback(SnackbarCallbackFragmentDelegate(presenter))
    }

    override fun inject() = viewModel.inject(this)

    override fun onCreate() {
        setContentView(R.layout.activity_root)

        joystick.view.setOnClickListener { onEscClick() }

        viewModel.showExitSnackbar.observeEvent(this) {
            sbExit.show()
        }

        setOrientation(viewModel.setOrientation.data!!)
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (fragment is ExplorerFragment) {
            explorerFragment = fragment
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when  {
            super.onKeyDown(keyCode, event) -> Unit
            keyCode == KeyEvent.KEYCODE_ESCAPE -> onEscClick()
            explorerFragment.onKeyDown(keyCode) -> Unit
            else -> return false
        }
        return true
    }

    override fun setTheme(resId: Int) {
        super.setTheme(resId)
        root {
            setBackgroundColor(findColorByAttr(R.attr.colorBackground))
        }
        joystick {
            setComposition()
        }
    }

    override fun onSubscribeData(owner: LifecycleOwner) {
        viewModel.setTheme.observeData(owner, ::setTheme)
        viewModel.setOrientation.observeData(owner, ::setOrientation)
        viewModel.setJoystick.observe(owner, Observer { joystick { setComposition(it) } })
    }

    override fun onBackPressed() = presenter.onBackButtonClick()

    private fun onEscClick() {
        val viewWithFocus = root.view.findFocus() as? EditText
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