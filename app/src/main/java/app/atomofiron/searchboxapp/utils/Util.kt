package app.atomofiron.searchboxapp.utils

import android.app.NotificationChannel
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.Screen
import java.util.*
import kotlin.math.max

object Util {
    @JvmStatic
    fun poop(message: String) {
        Log.e("atomofiron", message)
    }

    fun fixChannel(context: Context, id: String, name: String, importance: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = NotificationManagerCompat.from(context)
            var channel = manager.getNotificationChannel(id)
            if (channel == null) {
                channel = NotificationChannel(id, name, importance)
                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun getFormat(path: String): String {
        var index = path.lastIndexOf('/')
        index = max(0, index)
        index = path.lastIndexOf('.', index)
        return path.substring(index.inc()).lowercase()
    }

    fun Number.toHumanReadable(suffixes: Array<String?>): String {
        var order = 0
        var byteCount = toDouble()
        while (byteCount >= 970) {
            byteCount /= 1024f
            order++
        }
        return String.format(Locale.US, "%1$.2f %2\$s", byteCount, suffixes[order]).replace("[.,]00|(?<=[.,][0-9])0".toRegex(), "")
    }

    fun Resources.getSize(size: Int): Screen = when {
        size < getDimensionPixelSize(R.dimen.screen_compact) -> Screen.Compact
        size < getDimensionPixelSize(R.dimen.screen_medium) -> Screen.Medium
        else -> Screen.Expanded
    }
}