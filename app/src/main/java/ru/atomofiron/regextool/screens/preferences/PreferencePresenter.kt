package ru.atomofiron.regextool.screens.preferences

import app.atomofiron.common.arch.BasePresenter
import leakcanary.AppWatcher
import ru.atomofiron.regextool.screens.preferences.fragment.ExportImportFragmentDelegate
import ru.atomofiron.regextool.screens.preferences.fragment.JoystickFragmentDelegate
import ru.atomofiron.regextool.screens.preferences.fragment.PreferenceUpdateOutput
import ru.atomofiron.regextool.screens.preferences.presenter.ExportImportPresenterDelegate
import ru.atomofiron.regextool.screens.preferences.presenter.JoystickPresenterDelegate
import ru.atomofiron.regextool.screens.preferences.presenter.PreferenceUpdatePresenterDelegate

class PreferencePresenter(
        viewModel: PreferenceViewModel,
        override val router: PreferenceRouter,
        joystickDelegate: JoystickPresenterDelegate,
        exportImportDelegate: ExportImportPresenterDelegate,
        preferenceUpdateDelegate: PreferenceUpdatePresenterDelegate
) : BasePresenter<PreferenceViewModel, PreferenceRouter>(viewModel),
        JoystickFragmentDelegate.JoystickPreferenceOutput by joystickDelegate,
        ExportImportFragmentDelegate.ExportImportOutput by exportImportDelegate,
        PreferenceUpdateOutput by preferenceUpdateDelegate
{
    fun onLeakCanaryClick(isChecked: Boolean) {
        AppWatcher.config = AppWatcher.config.copy(enabled = isChecked)
    }
}