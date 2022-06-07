package app.atomofiron.searchboxapp.screens.preferences

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.ExportImportFragmentDelegate
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceClickOutput
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceUpdateOutput

class PreferencePresenter(
    viewModel: PreferenceViewModel,
    router: PreferenceRouter,
    exportImportDelegate: ExportImportFragmentDelegate.ExportImportOutput,
    preferenceUpdateDelegate: PreferenceUpdateOutput,
    preferenceClickOutput: PreferenceClickOutput,
) : BasePresenter<PreferenceViewModel, PreferenceRouter>(viewModel, router),
    ExportImportFragmentDelegate.ExportImportOutput by exportImportDelegate,
    PreferenceUpdateOutput by preferenceUpdateDelegate,
    PreferenceClickOutput by preferenceClickOutput
{

    override fun onSubscribeData() = Unit
}