package app.atomofiron.searchboxapp.screens.preferences.fragment

interface PreferenceUpdateOutput {
    fun onPreferenceUpdate(key: String, value: Int)
    fun onPreferenceUpdate(key: String, value: Long)
    fun onPreferenceUpdate(key: String, value: Boolean): Boolean
    fun onPreferenceUpdate(key: String, value: String)
    fun onPreferenceUpdate(key: String, value: Set<String>)
}