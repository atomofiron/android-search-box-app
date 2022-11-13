package app.atomofiron.searchboxapp.screens.main

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.injectable.service.WindowService
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.main.fragment.SnackbarCallbackFragmentDelegate
import app.atomofiron.searchboxapp.screens.main.presenter.AppEventDelegate
import app.atomofiron.searchboxapp.screens.main.presenter.AppEventDelegateApi
import app.atomofiron.searchboxapp.screens.main.util.tasks.XTask
import app.atomofiron.searchboxapp.utils.Shell

class MainPresenter(
    private val viewModel: MainViewModel,
    private val router: MainRouter,
    private val windowService: WindowService,
    appStore: AppStore,
    preferenceStore: PreferenceStore,
) : SnackbarCallbackFragmentDelegate.SnackbarCallbackOutput,
    AppEventDelegateApi by AppEventDelegate(viewModel.viewModelScope, appStore, preferenceStore)
{
    override var isExitSnackbarShown: Boolean = false
    private val scope = viewModel.viewModelScope

    init {
        preferenceStore.appTheme.collect(scope) { theme ->
            viewModel.sendTheme(theme)
        }
        preferenceStore.deepBlack.collect(scope) { deepBlack ->
            val appTheme = preferenceStore.appTheme.value
            viewModel.sendTheme(appTheme.copy(deepBlack))
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

    fun onEscClick() = when {
        router.onBack() -> Unit
        else -> viewModel.showExitSnackbar()
    }

    fun onExitClick() = router.closeApp()

    fun onBackButtonClick() = when {
        router.onBack() -> Unit
        isExitSnackbarShown -> router.closeApp()
        else -> viewModel.showExitSnackbar()
    }

    fun applyTheme(isDarkTheme: Boolean) {
        router.reattachFragments()
        updateLightStatusBar(isDarkTheme)
        updateLightNavigationBar(isDarkTheme)
    }

    fun updateLightNavigationBar(isDarkTheme: Boolean) {
        windowService.setLightNavigationBar(!isDarkTheme)
    }

    fun updateLightStatusBar(isDarkTheme: Boolean) {
        val lightStatusBar = router.lastVisibleFragment?.isLightStatusBar ?: !isDarkTheme
        windowService.setLightStatusBar(lightStatusBar)
    }
}