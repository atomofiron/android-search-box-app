package app.atomofiron.searchboxapp.screens.preferences

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.searchboxapp.screens.preferences.fragment.ExportImportFragmentDelegate
import app.atomofiron.searchboxapp.screens.preferences.fragment.JoystickFragmentDelegate
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceUpdateOutput
import app.atomofiron.searchboxapp.screens.preferences.presenter.ExportImportPresenterDelegate
import app.atomofiron.searchboxapp.screens.preferences.presenter.JoystickPresenterDelegate
import app.atomofiron.searchboxapp.screens.preferences.presenter.PreferenceUpdatePresenterDelegate
import app.atomofiron.searchboxapp.utils.AppWatcherProxy

class PreferencePresenter(
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
        AppWatcherProxy.setEnabled(isChecked)
        /*GlobalScope.launch {
            logD("offer $c")
            RootChannel.channel.offer(c++)
        }*/
    }

    override fun onSubscribeData() = Unit
}