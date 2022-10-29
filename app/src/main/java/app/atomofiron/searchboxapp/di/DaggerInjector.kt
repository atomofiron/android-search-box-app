package app.atomofiron.searchboxapp.di

import android.app.Application
import app.atomofiron.searchboxapp.utils.AppWatcherProxy

object DaggerInjector {

    lateinit var appComponent: AppComponent

    fun init(application: Application) {
        appComponent = DaggerAppComponent
            .builder()
            .appContext(application.applicationContext)
            .appWatcher(AppWatcherProxy())
            .assetManager(application.assets)
            .build()
    }
}
