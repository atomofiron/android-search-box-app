package app.atomofiron.searchboxapp.screens.preferences

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.flow.dataFlow
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.android.App
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.model.preference.JoystickComposition
import app.atomofiron.searchboxapp.model.preference.ToyboxVariant
import app.atomofiron.searchboxapp.utils.Shell
import javax.inject.Inject

class PreferenceViewModel : BaseViewModel<PreferenceComponent, PreferenceFragment, PreferencePresenter>() {

    @Inject
    lateinit var preferenceStore: PreferenceStore

    val alert = dataFlow<String>(single = true)
    val alertOutputSuccess = dataFlow<Int>(single = true)
    val alertOutputError = dataFlow<Shell.Output>(single = true)
    val showDeepBlack = dataFlow<Boolean>()
    val isExportImportAvailable: Boolean get() = App.appContext.getExternalFilesDir(null) != null
    val explorerItemComposition: ExplorerItemComposition get() = preferenceStore.explorerItemComposition.entity
    val joystickComposition: JoystickComposition get() = preferenceStore.joystickComposition.entity
    val toyboxVariant: ToyboxVariant get() = preferenceStore.toyboxVariant.entity

    @Inject
    override lateinit var presenter: PreferencePresenter

    override fun inject(view: PreferenceFragment) {
        super.inject(view)
        component.inject(this)
    }

    override fun createComponent(fragmentProperty: WeakProperty<Fragment>) = DaggerPreferenceComponent
        .builder()
        .bind(this)
        .bind(fragmentProperty)
        .dependencies(DaggerInjector.appComponent)
        .build()

    fun getCurrentValue(key: String): Any? = preferenceStore.getCurrentValue(key)
}