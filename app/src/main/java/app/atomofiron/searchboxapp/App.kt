package app.atomofiron.searchboxapp

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import leakcanary.AppWatcher
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.work.NotificationWorker
import javax.inject.Inject

class App : Application(), Configuration.Provider {
    companion object {
        lateinit var appContext: Context
    }

    @Inject
    lateinit var workManager: WorkManager

    override fun onCreate() {
        super.onCreate()

        appContext = applicationContext

        DaggerInjector.init(applicationContext)
        DaggerInjector.appComponent.inject(this)
        workManager.cancelUniqueWork(NotificationWorker.NAME)

        AppWatcher.config = AppWatcher.config.copy(enabled = false)

        if (BuildConfig.YANDEX_API_KEY != null) {
            val config = YandexMetricaConfig.newConfigBuilder(BuildConfig.YANDEX_API_KEY)
                    .withLocationTracking(false)
                    .withCrashReporting(true)
                    .build()
            YandexMetrica.activate(applicationContext, config)
        }
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder().build()
}