package app.atomofiron.searchboxapp.utils

object AppWatcherProxy {
    fun isEnabled(): Boolean = false//leakcanary.AppWatcher.config.enabled

    fun setEnabled(enabled: Boolean) {
        //leakcanary.AppWatcher.config = leakcanary.AppWatcher.config.copy(enabled = enabled)
    }
}