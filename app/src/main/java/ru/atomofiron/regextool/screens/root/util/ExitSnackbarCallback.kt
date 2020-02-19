package ru.atomofiron.regextool.screens.root.util

import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import ru.atomofiron.regextool.screens.root.RootViewModel

class ExitSnackbarCallback(private val viewModel: RootViewModel) : BaseTransientBottomBar.BaseCallback<Snackbar>() {

    override fun onShown(transientBottomBar: Snackbar?) {
        viewModel.sbExitIsShown = true
    }
    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
        viewModel.sbExitIsShown = false
    }
}