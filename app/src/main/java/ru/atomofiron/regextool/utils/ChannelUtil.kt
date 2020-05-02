package ru.atomofiron.regextool.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

object ChannelUtil {
    private var id: String? = null
    private var name: String? = null
    private var importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT

    fun id(id: String): ChannelUtil {
        this.id = id
        return this
    }

    fun name(name: String): ChannelUtil {
        this.name = name
        return this
    }

    fun importance(importance: Int): ChannelUtil {
        this.importance = importance
        return this
    }

    @SuppressLint("WrongConstant")
    fun fix(context: Context) {
        val id = id!!
        val name = name!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = NotificationManagerCompat.from(context)
            var channel = manager.getNotificationChannel(id)
            if (channel == null) {
                channel = NotificationChannel(id, name, importance)
                manager.createNotificationChannel(channel)
            }
        }
    }
}