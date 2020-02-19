package ru.atomofiron.regextool.screens.root

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.base.BaseActivity
import ru.atomofiron.regextool.common.util.Knife
import ru.atomofiron.regextool.screens.root.util.ExitSnackbarCallback
import kotlin.reflect.KClass

open class RootActivityWhite : BaseActivity<RootViewModel>() {

    override val viewModelClass: KClass<RootViewModel> = RootViewModel::class

    private val joystick = Knife<View>(this, R.id.root_iv_joystick)
    private val anchor = Knife<View>(this, R.id.root_v_anchor)
    private lateinit var sbExit: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_root)
        super.onCreate(savedInstanceState)

        joystick.view.setOnClickListener {
            viewModel.onJoystickClick()
        }

        viewModel.showExitSnackbar.observeEvent(this) {
            sbExit.show()
        }

        sbExit = Snackbar.make(joystick.view, R.string.click_back_to_exit, Snackbar.LENGTH_SHORT)
                .setAnchorView(anchor.view)
                .setActionTextColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setAction(R.string.exit) { viewModel.onExitClick() }
                .addCallback(ExitSnackbarCallback(viewModel))
    }

    // todo onNewIntent ACTION_SHOW_RESULT, ACTION_SHOW_RESULTS
    // todo setRequestedOrientation Const.PREF_ORIENTATION
}