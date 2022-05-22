package app.atomofiron.searchboxapp.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Configuration
import androidx.work.WorkManager
import app.atomofiron.searchboxapp.BuildConfig
import app.atomofiron.searchboxapp.R
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.utils.AppWatcherProxy
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.getMarketIntent
import app.atomofiron.searchboxapp.work.NotificationWorker
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
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

        AppWatcherProxy.setEnabled(false)

        if (BuildConfig.YANDEX_API_KEY != null) {
            val config = YandexMetricaConfig.newConfigBuilder(BuildConfig.YANDEX_API_KEY)
                    .withLocationTracking(false)
                    .withCrashReporting(true)
                    .build()
            YandexMetrica.activate(applicationContext, config)
        }
        checkForUpdate(this)
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder().build()

    private fun checkForUpdate(context: Context) {
        val appUpdateManager = AppUpdateManagerFactory.create(context)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            val availability = appUpdateInfo.updateAvailability()
            val isAvailable = availability == UpdateAvailability.UPDATE_AVAILABLE
            when {
                isAvailable -> showNotificationForUpdate()
                !BuildConfig.DEBUG -> {
                    val throwable = Throwable("${Const.ERROR_UPDATE_AVAILABILITY} Availability: $availability")
                    YandexMetrica.reportError(Const.ERROR_UPDATE_AVAILABILITY, throwable)
                }
            }
        }
        appUpdateInfoTask.addOnFailureListener {
            if (!BuildConfig.DEBUG) {
                YandexMetrica.reportError(Const.ERROR_CHECK_UPDATE, Throwable(it.message))
            }
        }
    }

    private fun showNotificationForUpdate() {
        val notificationManager = NotificationManagerCompat.from(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel = notificationManager.getNotificationChannel(Const.NOTIFICATION_CHANNEL_UPDATE_ID)
            if (channel == null) {
                channel = NotificationChannel(
                        Const.NOTIFICATION_CHANNEL_UPDATE_ID,
                        getString(R.string.channel_name_updates),
                        NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }
        }
        val notificationIntent = PendingIntent
                .getActivity(this, Const.REQUEST_CODE_MARKET_UPDATE, getMarketIntent(), PendingIntent.FLAG_UPDATE_CURRENT)
        val actionIntent = PendingIntent
                .getActivity(this, Const.REQUEST_CODE_MARKET_UPDATE, getMarketIntent(), PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(this, Const.NOTIFICATION_CHANNEL_UPDATE_ID)
                .setTicker(getString(R.string.update_available))
                .setContentTitle(getString(R.string.update_available))
                .setSmallIcon(R.drawable.ic_notification_update)
                .setContentIntent(notificationIntent)
                .addAction(0, getString(R.string.get_update), actionIntent)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimaryLight))
                .build()

        notificationManager.notify(Const.NOTIFICATION_ID_UPDATE, notification)
    }
}