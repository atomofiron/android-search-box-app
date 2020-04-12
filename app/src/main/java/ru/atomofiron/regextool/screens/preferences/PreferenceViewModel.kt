package ru.atomofiron.regextool.screens.preferences

import android.app.Application
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.SingleLiveEvent
import ru.atomofiron.regextool.App
import ru.atomofiron.regextool.di.DaggerInjector
import ru.atomofiron.regextool.injectable.store.SettingsStore
import ru.atomofiron.regextool.model.ExplorerItemComposition
import ru.atomofiron.regextool.model.JoystickComposition
import ru.atomofiron.regextool.utils.Shell
import javax.inject.Inject

class PreferenceViewModel(app: Application) : BaseViewModel<PreferenceComponent, PreferenceFragment>(app) {

    @Inject
    lateinit var settingsStore: SettingsStore

    val alert = SingleLiveEvent<String>()
    val alertOutputSuccess = SingleLiveEvent<Int>()
    val alertOutputError = SingleLiveEvent<Shell.Output>()
    val isExportImportAvailable: Boolean get() = getApplication<App>().applicationContext.getExternalFilesDir(null) != null
    val explorerItemComposition: ExplorerItemComposition get() = settingsStore.explorerItemComposition.entity
    val joystickComposition: JoystickComposition get() = settingsStore.joystickComposition.entity

    override val component = DaggerPreferenceComponent
            .builder()
            .bind(this)
            .bind(viewProperty)
            .dependencies(DaggerInjector.appComponent)
            .build()

    override fun inject(view: PreferenceFragment) {
        super.inject(view)
        component.inject(this)
        component.inject(view)
    }

    fun getCurrentValue(key: String): Any? = settingsStore.getCurrentValue(key)
}