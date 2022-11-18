package app.atomofiron.searchboxapp.screens.main

import app.atomofiron.common.util.flow.*
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.main.util.tasks.XTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewState(
    private val scope: CoroutineScope,
    val preferenceStore: PreferenceStore,
) {

    val showExitSnackbar = ChannelFlow<Unit>()
    val setTheme = preferenceStore.appTheme
    val setOrientation = preferenceStore.appOrientation
    val setJoystick = preferenceStore.joystickComposition
    val tasks = MutableStateFlow<List<XTask>>(listOf())

    fun showExitSnackbar() = showExitSnackbar.invoke(scope)
}