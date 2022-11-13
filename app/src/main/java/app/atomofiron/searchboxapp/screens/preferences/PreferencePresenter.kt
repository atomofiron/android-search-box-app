package app.atomofiron.searchboxapp.screens.preferences

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.ExportImportDelegate
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceClickOutput
import app.atomofiron.searchboxapp.utils.Shell
import kotlinx.coroutines.launch

class PreferencePresenter(
    viewModel: PreferenceViewModel,
    router: PreferenceRouter,
    exportImportDelegate: ExportImportDelegate.ExportImportOutput,
    preferenceClickOutput: PreferenceClickOutput,
    private val preferenceStore: PreferenceStore,
    private val appStore: AppStore,
) : BasePresenter<PreferenceViewModel, PreferenceRouter>(viewModel, router),
    ExportImportDelegate.ExportImportOutput by exportImportDelegate,
    PreferenceClickOutput by preferenceClickOutput
{

    val resources by appStore.resourcesProperty

    init {
        onSubscribeData()
    }

    override fun onSubscribeData() {
        viewModel.viewModelScope.launch {
            preferenceStore.useSu.collect {
                if (it) onUseSuEnabled()
            }
        }
    }

    private suspend fun onUseSuEnabled() {
        val output = Shell.checkSu()
        if (!output.success) {
            preferenceStore.setUseSu(false)
            val message = when {
                output.error.isNotBlank() -> output.error
                else -> resources.getString(R.string.not_allowed)
            }
            viewModel.showAlert(message)
        }
    }
}