package app.atomofiron.searchboxapp.screens.preferences

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.model.preference.JoystickComposition
import app.atomofiron.searchboxapp.model.preference.ToyboxVariant
import app.atomofiron.searchboxapp.utils.Shell
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class PreferenceViewModel : BaseViewModel<PreferenceComponent, PreferenceFragment, PreferencePresenter>() {

    @Inject
    lateinit var preferenceStore: PreferenceStore
    @SuppressLint("StaticFieldLeak")
    @Inject
    lateinit var appContext: Context

    val alert = ChannelFlow<String>()
    val alertOutputSuccess = ChannelFlow<Int>()
    val alertOutputError = ChannelFlow<Shell.Output>()
    val showDeepBlack = MutableStateFlow(false)
    val isExportImportAvailable: Boolean get() = appContext.getExternalFilesDir(null) != null
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

    fun showAlert(value: String) {
        alert[viewModelScope] = value
    }

    fun sendAlertOutputSuccess(value: Int) {
        alertOutputSuccess[viewModelScope] = value
    }

    fun sendAlertOutputError(value: Shell.Output) {
        alertOutputError[viewModelScope] = value
    }
}