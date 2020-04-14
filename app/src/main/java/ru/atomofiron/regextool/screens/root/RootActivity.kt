package ru.atomofiron.regextool.screens.root

import android.view.KeyEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import app.atomofiron.common.arch.BaseActivity
import app.atomofiron.common.util.Knife
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.Joystick
import ru.atomofiron.regextool.model.AppOrientation
import ru.atomofiron.regextool.screens.explorer.ExplorerFragment
import ru.atomofiron.regextool.screens.root.fragment.SnackbarCallbackFragmentDelegate
import ru.atomofiron.regextool.screens.root.util.SnackbarWrapper
import javax.inject.Inject
import kotlin.reflect.KClass

open class RootActivity : BaseActivity<RootViewModel, RootPresenter>() {

    override val viewModelClass: KClass<RootViewModel> = RootViewModel::class

    @Inject
    override lateinit var presenter: RootPresenter

    private val root = Knife<ConstraintLayout>(this, R.id.root_cl_root)
    private val joystick = Knife<Joystick>(this, R.id.root_iv_joystick)

    private val sbExit: SnackbarWrapper = SnackbarWrapper(this) {
        Snackbar.make(joystick.view, R.string.click_back_to_exit, Snackbar.LENGTH_SHORT)
                .setAnchorView(joystick.view)
                .setActionTextColor(this@RootActivity.findColorByAttr(R.attr.colorAccent))
                .setAction(R.string.exit) { presenter.onExitClick() }
                .addCallback(SnackbarCallbackFragmentDelegate(presenter))
    }

    override fun inject() = viewModel.inject(this)

    override fun onCreate() {
        setContentView(R.layout.activity_root)

        joystick.view.setOnClickListener {
            val viewWithFocus = root.view.findFocus()
            val consumed = viewWithFocus?.hideKeyboard() != null
            if (!consumed) {
                presenter.onJoystickClick()
            }
        }

        viewModel.showExitSnackbar.observeEvent(this) {
            sbExit.show()
        }

        setOrientation(viewModel.setOrientation.data!!)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val fragment = supportFragmentManager.fragments.find { it is ExplorerFragment } as ExplorerFragment?
        val consumed = fragment?.onKeyDown(keyCode) == true
        return consumed || super.onKeyDown(keyCode, event)
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

    private fun setOrientation(orientation: AppOrientation) {
        if (requestedOrientation != orientation.constant) {
            requestedOrientation = orientation.constant
        }
    }

    // todo onNewIntent ACTION_SHOW_RESULT, ACTION_SHOW_RESULTS
    // todo setRequestedOrientation Const.PREF_ORIENTATION
}