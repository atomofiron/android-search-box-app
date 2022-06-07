package app.atomofiron.searchboxapp.screens.preferences.fragment

interface PreferenceClickOutput {
    fun onLeakCanaryClick(isChecked: Boolean)
    fun onAboutClick()
    fun onExportImportClick()
    fun onExplorerItemClick()
    fun onJoystickClick()
    fun onToyboxClick()
}