package app.atomofiron.searchboxapp.screens.preferences

import app.atomofiron.common.arch.BasePresenter
import kotlinx.coroutines.CoroutineScope
import leakcanary.AppWatcher
import app.atomofiron.searchboxapp.screens.preferences.fragment.ExportImportFragmentDelegate
import app.atomofiron.searchboxapp.screens.preferences.fragment.JoystickFragmentDelegate
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceUpdateOutput
import app.atomofiron.searchboxapp.screens.preferences.presenter.ExportImportPresenterDelegate
import app.atomofiron.searchboxapp.screens.preferences.presenter.JoystickPresenterDelegate
import app.atomofiron.searchboxapp.screens.preferences.presenter.PreferenceUpdatePresenterDelegate

class PreferencePresenter(
        val scope: CoroutineScope,
        viewModel: PreferenceViewModel,
        router: PreferenceRouter,
        joystickDelegate: JoystickPresenterDelegate,
        exportImportDelegate: ExportImportPresenterDelegate,
        preferenceUpdateDelegate: PreferenceUpdatePresenterDelegate
) : BasePresenter<PreferenceViewModel, PreferenceRouter>(viewModel, router),
        JoystickFragmentDelegate.JoystickPreferenceOutput by joystickDelegate,
        ExportImportFragmentDelegate.ExportImportOutput by exportImportDelegate,
        PreferenceUpdateOutput by preferenceUpdateDelegate
{

    // test
    /*var c = 0
    init {
        logD("init")
        scope.launch {
            val flow = RootChannel.channel.asFlow()
            flow.collect {
                logD("a $it")
            }
        }
        scope.launch {
            val flow = RootChannel.channel.asFlow()
            flow.collect {
                logD("b $it")
            }
        }
    }*/

    fun onLeakCanaryClick(isChecked: Boolean) {
        AppWatcher.config = AppWatcher.config.copy(enabled = isChecked)
        /*GlobalScope.launch {
            logD("offer $c")
            RootChannel.channel.offer(c++)
        }*/
    }
}