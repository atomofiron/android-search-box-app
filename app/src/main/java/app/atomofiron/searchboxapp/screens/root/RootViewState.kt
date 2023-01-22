package app.atomofiron.searchboxapp.screens.root

import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.screens.main.fragment.SnackbarCallbackFragmentDelegate.SnackbarCallbackOutput
import kotlinx.coroutines.CoroutineScope

class RootViewState(
    private val scope: CoroutineScope,
) {

    val showExitSnackbar = ChannelFlow<SnackbarCallbackOutput>()

    fun showExitSnackbar(listener: SnackbarCallbackOutput) {
        showExitSnackbar[scope] = listener
    }
}