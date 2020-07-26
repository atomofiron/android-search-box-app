package app.atomofiron.searchboxapp.screens.root

import androidx.lifecycle.MutableLiveData
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.LateinitLiveData
import app.atomofiron.common.util.SingleLiveEvent
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.model.preference.AppOrientation
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.model.preference.JoystickComposition
import app.atomofiron.searchboxapp.screens.root.util.tasks.XTask

class RootViewModel : BaseViewModel<RootComponent, RootActivity>() {

    val showExitSnackbar = SingleLiveEvent<Unit>()
    val setTheme = SingleLiveEvent<AppTheme>()
    val setOrientation = SingleLiveEvent<AppOrientation>()
    val setJoystick = LateinitLiveData<JoystickComposition>()
    val tasks = MutableLiveData<List<XTask>>()

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