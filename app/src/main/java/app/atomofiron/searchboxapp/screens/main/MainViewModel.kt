package app.atomofiron.searchboxapp.screens.main

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.atomofiron.common.util.flow.*
import app.atomofiron.common.util.property.MutableWeakProperty
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.model.preference.AppOrientation
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.model.preference.JoystickComposition
import app.atomofiron.searchboxapp.screens.main.util.tasks.XTask
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel : ViewModel() {
    private var component: MainComponent? = null
    private val activityProperty = MutableWeakProperty<FragmentActivity>()

    val showExitSnackbar = ChannelFlow<Unit>()
    val setTheme = ChannelFlow<AppTheme>()
    val setOrientation = MutableStateFlow(AppOrientation.UNDEFINED)
    val setJoystick = DeferredStateFlow<JoystickComposition>()
    val tasks = MutableStateFlow<List<XTask>>(listOf())

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

    fun showExitSnackbar() = showExitSnackbar.invoke(viewModelScope)

    fun sendTheme(value: AppTheme) {
        setTheme[viewModelScope] = value
    }
}