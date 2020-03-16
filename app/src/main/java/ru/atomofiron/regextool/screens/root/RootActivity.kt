package ru.atomofiron.regextool.screens.root

import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import app.atomofiron.common.base.BaseActivity
import app.atomofiron.common.util.Knife
import app.atomofiron.common.util.LazyReincarnation
import com.google.android.material.snackbar.Snackbar
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.model.AppOrientation
import ru.atomofiron.regextool.screens.root.util.ExitSnackbarCallback
import ru.atomofiron.regextool.screens.root.util.tasks.TasksSheetView
import ru.atomofiron.regextool.view.custom.Joystick
import kotlin.reflect.KClass

open class RootActivity : BaseActivity<RootViewModel>() {

    override val viewModelClass: KClass<RootViewModel> = RootViewModel::class

    private val root = Knife<CoordinatorLayout>(this, R.id.root_cl_root)
    private val joystick = Knife<Joystick>(this, R.id.root_iv_joystick)
    private val tsvTasks = Knife<TasksSheetView>(this, R.id.root_tsv_tasks)

    private val sbExit: LazyReincarnation<Snackbar> = LazyReincarnation {
        Snackbar.make(joystick.view, R.string.click_back_to_exit, Snackbar.LENGTH_SHORT)
                .setAnchorView(joystick.view)
                .setActionTextColor(ContextCompat.getColor(this@RootActivity, R.color.colorAccent))
                .setAction(R.string.exit) { viewModel.onExitClick() }
                .addCallback(ExitSnackbarCallback(viewModel))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_root)
        super.onCreate(savedInstanceState)

        joystick.view.setOnClickListener {
            when (onBack()) {
                true -> Unit
                else -> viewModel.onJoystickClick()
            }
        }

        viewModel.showExitSnackbar.observeEvent(this) {
            sbExit { show() }
        }

        tsvTasks {
            setTrackingView(joystick.view)
        }

        setOrientation(viewModel.setOrientation.data!!)
    }

    override fun setTheme(resId: Int) {
        super.setTheme(resId)
        sbExit.wipe()
        tsvTasks {
            resetContentView()
        }
    }

    override fun onSubscribeData(owner: LifecycleOwner) {
        viewModel.setTheme.observeData(owner, ::setTheme)
        viewModel.setOrientation.observeData(owner, ::setOrientation)
        viewModel.tasks.observe(owner, Observer { tsvTasks { setItems(it) } })
    }

    private fun setOrientation(orientation: AppOrientation) {
        if (requestedOrientation != orientation.constant) {
            requestedOrientation = orientation.constant
        }
    }

    private fun onBack(): Boolean = tsvTasks(default = false) { hide() }

    override fun onBackPressed() {
        when (onBack()) {
            true -> Unit
            else -> super.onBackPressed()
        }
    }

    // todo onNewIntent ACTION_SHOW_RESULT, ACTION_SHOW_RESULTS
    // todo setRequestedOrientation Const.PREF_ORIENTATION
}