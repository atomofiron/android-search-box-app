package ru.atomofiron.regextool

import android.app.Application
import android.content.Context
import androidx.work.WorkManager
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import leakcanary.AppWatcher
import ru.atomofiron.regextool.di.DaggerInjector
import ru.atomofiron.regextool.work.NotificationWorker
import javax.inject.Inject

class App : Application() {
    companion object {
        lateinit var appContext: Context
        val pathToybox32: String get() = "${appContext.filesDir}/toybox32"
        val pathToybox64: String get() = "${appContext.filesDir}/toybox64"
    }

    @Inject
    lateinit var workManager: WorkManager

    override fun onCreate() {
        super.onCreate()

        appContext = applicationContext

        DaggerInjector.init(this)
        DaggerInjector.appComponent.inject(this)
        workManager.cancelUniqueWork(NotificationWorker.NAME)

        AppWatcher.config = AppWatcher.config.copy(enabled = false)

        if (!BuildConfig.DEBUG) {
            val config = YandexMetricaConfig.newConfigBuilder(BuildConfig.YANDEX_API_KEY)
                    .withLocationTracking(false)
                    .withCrashReporting(true)
                    .build()
            YandexMetrica.activate(applicationContext, config);
        }
    }
}