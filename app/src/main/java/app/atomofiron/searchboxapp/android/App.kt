package app.atomofiron.searchboxapp.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Configuration
import androidx.work.WorkManager
import app.atomofiron.searchboxapp.BuildConfig
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.PreferenceKeys
import app.atomofiron.searchboxapp.utils.getMarketIntent
import app.atomofiron.searchboxapp.utils.immutable
import app.atomofiron.searchboxapp.work.NotificationWorker
import com.google.android.material.color.DynamicColors
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import javax.inject.Inject

class App : Application(), Configuration.Provider {
    companion object {
        private const val PRIVATE_PREFERENCES_NAME = "app_preferences"

        lateinit var instance: App
    }

    @Inject
    lateinit var workManager: WorkManager
    private lateinit var sp: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        instance = this
        sp = getSharedPreferences(PRIVATE_PREFERENCES_NAME, MODE_PRIVATE)

        DynamicColors.applyToActivitiesIfAvailable(this)

        DaggerInjector.init(this)
        DaggerInjector.appComponent.inject(this)
        workManager.cancelUniqueWork(NotificationWorker.NAME)

        val themeName = sp.getString(PreferenceKeys.KeyAppTheme.name, AppTheme.defaultName())
        setTheme(AppTheme.fromString(themeName))

        checkForUpdate(this)
    }

    fun setTheme(theme: AppTheme) {
        val mode = when(theme) {
            is AppTheme.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            is AppTheme.Light -> AppCompatDelegate.MODE_NIGHT_NO
            is AppTheme.Dark -> AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(mode)
        sp.edit()
            .putString(PreferenceKeys.KeyAppTheme.name, theme.name)
            .apply()
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
                    // reportError(Const.ERROR_UPDATE_AVAILABILITY, throwable)
                }
            }
        }
        appUpdateInfoTask.addOnFailureListener {
            if (!BuildConfig.DEBUG) {
                // reportError(Const.ERROR_CHECK_UPDATE, Throwable(it.message))
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
        val flag = PendingIntent.FLAG_UPDATE_CURRENT.immutable()
        val notificationIntent = PendingIntent.getActivity(this, Const.REQUEST_CODE_MARKET_UPDATE, getMarketIntent(), flag)
        val actionIntent = PendingIntent.getActivity(this, Const.REQUEST_CODE_MARKET_UPDATE, getMarketIntent(), flag)
        val notification = NotificationCompat.Builder(this, Const.NOTIFICATION_CHANNEL_UPDATE_ID)
                .setTicker(getString(R.string.update_available))
                .setContentTitle(getString(R.string.update_available))
                .setSmallIcon(R.drawable.ic_notification_update)
                .setContentIntent(notificationIntent)
                .addAction(0, getString(R.string.get_update), actionIntent)
                .setColor(ContextCompat.getColor(this, R.color.primary_light))
                .build()

        notificationManager.notify(Const.NOTIFICATION_ID_UPDATE, notification)
    }
}