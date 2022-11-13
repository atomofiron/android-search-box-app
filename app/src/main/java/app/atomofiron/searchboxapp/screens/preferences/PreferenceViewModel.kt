package app.atomofiron.searchboxapp.screens.preferences

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceDataStore
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.utils.Shell
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class PreferenceViewModel : BaseViewModel<PreferenceComponent, PreferenceFragment, PreferencePresenter>() {

    @Inject
    lateinit var preferenceDataStore: PreferenceDataStore
    @SuppressLint("StaticFieldLeak")
    @Inject
    lateinit var appContext: Context

    val alert = ChannelFlow<String>()
    val alertOutputSuccess = ChannelFlow<Int>()
    val alertOutputError = ChannelFlow<Shell.Output>()
    val showDeepBlack = MutableStateFlow(false)
    // todo zip and share the backup
    val isExportImportAvailable: Boolean get() = appContext.getExternalFilesDir(null) != null

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