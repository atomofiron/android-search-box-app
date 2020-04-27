package ru.atomofiron.regextool.android

import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.di.DaggerInjector
import ru.atomofiron.regextool.log2
import ru.atomofiron.regextool.screens.root.RootActivity
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.work.NotificationWorker
import java.util.*
import javax.inject.Inject

class ForegroundService : IntentService("NotificationService") {

    @Inject
    lateinit var workManager: WorkManager

    private lateinit var workUUID: UUID

    init {
        DaggerInjector.appComponent.inject(this)
    }

    override fun onCreate() {
        super.onCreate()
        log2("onCreate")
        startForeground()
        val request = OneTimeWorkRequest.from(NotificationWorker::class.java)
        val cont = workManager.beginWith(request)
        cont.enqueue()
        workUUID = request.id
    }

    override fun onDestroy() {
        super.onDestroy()
        log2("onDestroy")
        stopForeground(true)
        workManager.cancelWorkById(workUUID)
    }

    private fun startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.foreground_notification_name)
            val manager = NotificationManagerCompat.from(this)
            val channel = NotificationChannel(Const.FOREGROUND_NOTIFICATION_CHANNEL_ID, name, IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }
        val intent = Intent(this, RootActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, Const.FOREGROUND_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val color = ContextCompat.getColor(this, R.color.colorPrimaryLight)
        val notification = NotificationCompat.Builder(this, Const.FOREGROUND_NOTIFICATION_CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(getString(R.string.searching))
                .setSmallIcon(R.drawable.ic_search_file)
                .setColor(color)
                .setContentIntent(pendingIntent)
                .build()
        startForeground(Const.FOREGROUND_NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        log2("onBind")
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        log2("onUnbind")
        return super.onUnbind(intent)
    }

    override fun onHandleIntent(intent: Intent?) = Unit
}