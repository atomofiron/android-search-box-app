package app.atomofiron.searchboxapp.screens.main

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.atomofiron.common.util.flow.dataFlow
import app.atomofiron.common.util.property.MutableWeakProperty
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.model.preference.AppOrientation
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.model.preference.JoystickComposition
import app.atomofiron.searchboxapp.screens.main.util.tasks.XTask

class MainViewModel : ViewModel() {
    private var component: MainComponent? = null
    private val activityProperty = MutableWeakProperty<FragmentActivity>()

    val showExitSnackbar = dataFlow(Unit, single = true)
    val setTheme = dataFlow<AppTheme>(single = true)
    val setOrientation = dataFlow<AppOrientation>()
    val setJoystick = dataFlow<JoystickComposition>()
    val tasks = dataFlow<List<XTask>>()

    fun inject(activity: MainActivity) {
        activityProperty.value = activity

        component = component ?: DaggerMainComponent
            .builder()
            .bind(this)
            .bind(viewModelScope)
            .bind(activityProperty)
            .dependencies(DaggerInjector.appComponent)
            .build()

        component?.inject(activity)
    }
}