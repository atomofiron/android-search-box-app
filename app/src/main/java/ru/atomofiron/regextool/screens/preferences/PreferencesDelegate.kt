package ru.atomofiron.regextool.screens.preferences

class PreferencesDelegate(
        private val parentFragment: PreferenceFragment,
        private val viewModel: PreferenceViewModel
) : InternalPreferenceFragment.Provider, InternalPreferenceFragment.Output {

    override val isExportImportAvailable: Boolean get() = viewModel.isExportImportAvailable

    override fun getCurrentValue(key: String): Any? = viewModel.getCurrentValue(key)

    override fun onBack(): Boolean = parentFragment.onBack()

    override fun onExportImportClick() = parentFragment.onExportImportClick()

    override fun onPreferenceUpdate(key: String, value: Int): Boolean = viewModel.onPreferenceUpdate(key, value)

    override fun onPreferenceUpdate(key: String, value: String): Boolean = viewModel.onPreferenceUpdate(key, value)

    override fun onPreferenceUpdate(key: String, value: Boolean): Boolean = viewModel.onPreferenceUpdate(key, value)
}