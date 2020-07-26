package app.atomofiron.searchboxapp.utils

import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import java.util.*

object Util {
    @JvmStatic
    fun log9(message: String?) {
        Log.e("atomofiron", message)
    }

    fun isTextFile(path: String, extra: Array<String>): Boolean {
        var path = path
        path = getFormat(path)
        if (path.isEmpty()) return false
        when (path) {
            "txt", "java", "xml", "html", "htm", "smali", "log", "js", "css", "json", "kt" -> return true
        }
        for (s in extra) if (path == s) return true
        return false
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

    fun getFormat(path: String): String {
        var path = path
        var index = path.lastIndexOf('/')
        if (index == -1) {
            if (path.lastIndexOf('.') == -1) return path
        } else path = path.substring(index)
        index = path.lastIndexOf('.')
        return if (index == -1) "" else path.substring(index + 1).toLowerCase()
    }

    fun intToHumanReadable(bytes: Int, suffixes: Array<String?>): String {
        var order = 0
        var byteCount = bytes.toFloat()
        while (byteCount >= 970) {
            byteCount /= 1024f
            order++
        }
        return String.format(Locale.US, "%1$.2f %2\$s", byteCount, suffixes[order]).replace("[.,]00|(?<=[.,][0-9])0".toRegex(), "")
    }
}