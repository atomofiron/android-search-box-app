package app.atomofiron.searchboxapp.screens.preferences

import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.SingleLiveEvent
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.model.preference.JoystickComposition
import app.atomofiron.searchboxapp.model.preference.ToyboxVariant
import app.atomofiron.searchboxapp.utils.Shell
import javax.inject.Inject

class PreferenceViewModel : BaseViewModel<PreferenceComponent, PreferenceFragment>() {

    @Inject
    lateinit var preferenceStore: PreferenceStore

    val alert = SingleLiveEvent<String>()
    val alertOutputSuccess = SingleLiveEvent<Int>()
    val alertOutputError = SingleLiveEvent<Shell.Output>()
    val isExportImportAvailable: Boolean get() = context.getExternalFilesDir(null) != null
    val explorerItemComposition: ExplorerItemComposition get() = preferenceStore.explorerItemComposition.entity
    val joystickComposition: JoystickComposition get() = preferenceStore.joystickComposition.entity
    val toyboxVariant: ToyboxVariant get() = preferenceStore.toyboxVariant.entity

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

    fun getCurrentValue(key: String): Any? = preferenceStore.getCurrentValue(key)
}