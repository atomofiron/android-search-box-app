package app.atomofiron.searchboxapp.screens.main

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.value
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.main.fragment.SnackbarCallbackFragmentDelegate
import app.atomofiron.searchboxapp.screens.main.util.tasks.XTask
import app.atomofiron.searchboxapp.utils.Shell

class MainPresenter(
    private val viewModel: MainViewModel,
    private val router: MainRouter,
    preferenceStore: PreferenceStore
) : SnackbarCallbackFragmentDelegate.SnackbarCallbackOutput {
    override var isExitSnackbarShown: Boolean = false
    private val scope = viewModel.viewModelScope

    init {
        preferenceStore.appTheme.collect(scope) {
            viewModel.setTheme.value = it
            router.reattachFragments()
        }
        preferenceStore.appOrientation.collect(scope) {
            viewModel.setOrientation.value = it
        }
        preferenceStore.joystickComposition.collect(scope) {
            viewModel.setJoystick.value = it
        }
        preferenceStore.toyboxVariant.collect(scope) {
            Shell.toyboxPath = it.toyboxPath
        }
        viewModel.tasks.value = Array(16) { XTask() }.toList()
    }

    fun onJoystickClick() = when {
        router.onBack() -> Unit
        else -> viewModel.showExitSnackbar.invoke()
    }

    fun onExitClick() = router.closeApp()

    fun onBackButtonClick() = when {
        router.onBack() -> Unit
        isExitSnackbarShown -> router.closeApp()
        else -> viewModel.showExitSnackbar.invoke()
    }
}