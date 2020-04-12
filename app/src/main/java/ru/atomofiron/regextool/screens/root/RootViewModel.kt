package ru.atomofiron.regextool.screens.root

import android.app.Application
import androidx.lifecycle.MutableLiveData
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.LateinitLiveData
import app.atomofiron.common.util.SingleLiveEvent
import ru.atomofiron.regextool.di.DaggerInjector
import ru.atomofiron.regextool.model.AppOrientation
import ru.atomofiron.regextool.model.AppTheme
import ru.atomofiron.regextool.model.JoystickComposition
import ru.atomofiron.regextool.screens.root.util.tasks.XTask

class RootViewModel(app: Application) : BaseViewModel<RootComponent, RootActivity>(app) {

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