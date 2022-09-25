package app.atomofiron.searchboxapp.utils

//import leakcanary.AppWatcher

object AppWatcherProxy {
    fun isEnabled(): Boolean = false//AppWatcher.config.enabled

    fun setEnabled(enabled: Boolean) {
        //AppWatcher.config = AppWatcher.config.copy(enabled = enabled)
    }
}