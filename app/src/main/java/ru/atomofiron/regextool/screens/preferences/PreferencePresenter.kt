package ru.atomofiron.regextool.screens.preferences

import app.atomofiron.common.arch.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import leakcanary.AppWatcher
import ru.atomofiron.regextool.injectable.channel.RootChannel
import ru.atomofiron.regextool.logD
import ru.atomofiron.regextool.screens.preferences.fragment.ExportImportFragmentDelegate
import ru.atomofiron.regextool.screens.preferences.fragment.JoystickFragmentDelegate
import ru.atomofiron.regextool.screens.preferences.fragment.PreferenceUpdateOutput
import ru.atomofiron.regextool.screens.preferences.presenter.ExportImportPresenterDelegate
import ru.atomofiron.regextool.screens.preferences.presenter.JoystickPresenterDelegate
import ru.atomofiron.regextool.screens.preferences.presenter.PreferenceUpdatePresenterDelegate

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