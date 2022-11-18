package app.atomofiron.searchboxapp.screens.main

import app.atomofiron.common.util.flow.*
import app.atomofiron.searchboxapp.model.preference.AppOrientation
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.model.preference.JoystickComposition
import app.atomofiron.searchboxapp.screens.main.util.tasks.XTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewState(private val scope: CoroutineScope) {

    val showExitSnackbar = ChannelFlow<Unit>()
    val setTheme = ChannelFlow<AppTheme>()
    val setOrientation = MutableStateFlow(AppOrientation.UNDEFINED)
    val setJoystick = DeferredStateFlow<JoystickComposition>()
    val tasks = MutableStateFlow<List<XTask>>(listOf())

    fun showExitSnackbar() = showExitSnackbar.invoke(scope)

    fun sendTheme(value: AppTheme) {
        setTheme[scope] = value
    }
}