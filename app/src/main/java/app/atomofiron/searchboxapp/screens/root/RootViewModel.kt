package app.atomofiron.searchboxapp.screens.root

import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.flow.LiveDataFlow
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.model.preference.AppOrientation
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.model.preference.JoystickComposition
import app.atomofiron.searchboxapp.screens.root.util.tasks.XTask

class RootViewModel : BaseViewModel<RootComponent, RootActivity>() {

    val showExitSnackbar = LiveDataFlow(Unit, single = true)
    val setTheme = LiveDataFlow<AppTheme>(single = true)
    val setOrientation = LiveDataFlow<AppOrientation>(single = true)
    val setJoystick = LiveDataFlow<JoystickComposition>()
    val tasks = LiveDataFlow<List<XTask>>()

    override val component = DaggerRootComponent
            .builder()
            .bind(this)
            .bind(viewProperty)
            .dependencies(DaggerInjector.appComponent)
            .build()

    override fun inject(view: RootActivity) {
        super.inject(view)
        component.inject(view)
    }
}