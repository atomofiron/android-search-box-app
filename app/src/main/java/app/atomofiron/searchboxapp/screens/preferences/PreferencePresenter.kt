package app.atomofiron.searchboxapp.screens.preferences

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.ExportImportDelegate
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceClickOutput
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceUpdateOutput

class PreferencePresenter(
    viewModel: PreferenceViewModel,
    router: PreferenceRouter,
    exportImportDelegate: ExportImportDelegate.ExportImportOutput,
    preferenceUpdateDelegate: PreferenceUpdateOutput,
    preferenceClickOutput: PreferenceClickOutput,
) : BasePresenter<PreferenceViewModel, PreferenceRouter>(viewModel, router),
    ExportImportDelegate.ExportImportOutput by exportImportDelegate,
    PreferenceUpdateOutput by preferenceUpdateDelegate,
    PreferenceClickOutput by preferenceClickOutput
{

    override fun onSubscribeData() = Unit
}