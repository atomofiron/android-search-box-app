package ru.atomofiron.regextool.screens.preferences.delegate

import ru.atomofiron.regextool.screens.preferences.PreferenceFragment
import ru.atomofiron.regextool.screens.preferences.PreferenceViewModel

class PreferencesDelegate(
        private val parentFragment: PreferenceFragment,
        private val viewModel: PreferenceViewModel
) : InternalPreferenceFragment.Provider, InternalPreferenceFragment.Output {

    override val isExportImportAvailable: Boolean get() = viewModel.isExportImportAvailable

    override fun getCurrentValue(key: String): Any? = viewModel.getCurrentValue(key)

    override fun onBack(): Boolean = parentFragment.onBack()

    override fun onExportImportClick() = parentFragment.onExportImportClick()

    override fun onExplorerItemClick() = parentFragment.onExplorerItemClick()

    override fun onEscColorClick() = parentFragment.onEscColorClick()

    override fun onLeakCanaryClick() = viewModel.onLeakCanaryClick()

    override fun onPreferenceUpdate(key: String, value: Int): Boolean = viewModel.onPreferenceUpdate(key, value)

    override fun onPreferenceUpdate(key: String, value: String): Boolean = viewModel.onPreferenceUpdate(key, value)

    override fun onPreferenceUpdate(key: String, value: Boolean): Boolean = viewModel.onPreferenceUpdate(key, value)
}