package app.atomofiron.searchboxapp.screens.main

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.searchboxapp.injectable.channel.MainChannel
import app.atomofiron.searchboxapp.injectable.service.WindowService
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.main.fragment.SnackbarCallbackFragmentDelegate
import app.atomofiron.searchboxapp.screens.main.presenter.AppEventDelegate
import app.atomofiron.searchboxapp.screens.main.presenter.AppEventDelegateApi
import app.atomofiron.searchboxapp.screens.main.util.tasks.XTask
import kotlinx.coroutines.CoroutineScope

class MainPresenter(
    scope: CoroutineScope,
    private val viewState: MainViewState,
    router: MainRouter,
    private val windowService: WindowService,
    appStore: AppStore,
    private val preferenceStore: PreferenceStore,
    mainChannel: MainChannel,
) : BasePresenter<MainViewModel, MainRouter>(scope, router), SnackbarCallbackFragmentDelegate.SnackbarCallbackOutput,
    AppEventDelegateApi by AppEventDelegate(scope, router, appStore, preferenceStore, mainChannel)
{
    override var isExitSnackbarShown: Boolean = false

    init {
        viewState.tasks.value = Array(16) { XTask() }.toList()
    }

    override fun onSubscribeData() = Unit

    fun onEscClick() = when {
        router.onBack() -> Unit
        else -> viewState.showExitSnackbar()
    }

    fun onExitClick() = router.closeApp()

    fun onBackButtonClick() = when {
        router.onBack() -> Unit
        isExitSnackbarShown -> router.closeApp()
        else -> viewState.showExitSnackbar()
    }

    fun onThemeApplied(isDarkTheme: Boolean) {
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