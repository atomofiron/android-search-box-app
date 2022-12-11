package app.atomofiron.searchboxapp.android

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.logI
import app.atomofiron.searchboxapp.screens.main.MainActivity
import app.atomofiron.searchboxapp.updateNotificationChannel
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.work.NotificationWorker
import java.lang.Deprecated
import java.util.*
import javax.inject.Inject

class ForegroundService : IntentService("NotificationService") {
    companion object {
        private const val flag = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }

    @Inject
    lateinit var workManager: WorkManager

    private lateinit var workUUID: UUID

    init {
        DaggerInjector.appComponent.inject(this)
    }

    @Deprecated
    override fun onCreate() {
        super.onCreate()
        logI("onCreate")
        startForeground()
        val request = OneTimeWorkRequest.from(NotificationWorker::class.java)
        val cont = workManager.beginUniqueWork(NotificationWorker.NAME, ExistingWorkPolicy.REPLACE, request)
        cont.enqueue()
        workUUID = request.id
    }

    @Deprecated
    override fun onDestroy() {
        super.onDestroy()
        logI("onDestroy")
        stopForeground(true)
        workManager.cancelWorkById(workUUID)
    }

    private fun startForeground() {
        updateNotificationChannel(
            Const.FOREGROUND_NOTIFICATION_CHANNEL_ID,
            getString(R.string.foreground_notification_name),
            IMPORTANCE_LOW,
        )

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, Const.FOREGROUND_INTENT_REQUEST_CODE, intent, flag)
        val notification = NotificationCompat.Builder(this, Const.FOREGROUND_NOTIFICATION_CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(getString(R.string.searching))
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(findColorByAttr(R.attr.colorPrimary))
                .setContentIntent(pendingIntent)
                .build()
        startForeground(Const.FOREGROUND_NOTIFICATION_ID, notification)
    }

    @Deprecated
    override fun onBind(intent: Intent?): IBinder? {
        logI("onBind")
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        logI("onUnbind")
        return super.onUnbind(intent)
    }

    @Deprecated
    override fun onHandleIntent(intent: Intent?) = Unit
}