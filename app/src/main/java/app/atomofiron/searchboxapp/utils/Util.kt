package app.atomofiron.searchboxapp.utils

import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import java.util.*
import kotlin.math.max

object Util {
    @JvmStatic
    fun poop(message: String) {
        Log.e("atomofiron", message)
    }

    fun isTextFile(path: String, extra: Array<String>): Boolean {
        val ext = getFormat(path)
        if (ext.isEmpty()) return false
        when (ext) {
            "txt", "java", "xml", "html", "htm", "smali", "log", "js", "css", "json", "kt" -> return true
        }
        for (s in extra) if (ext == s) return true
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
}