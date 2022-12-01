package app.atomofiron.searchboxapp.utils

import android.content.Context
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.min

object Tool {
    fun getExternalStorageDirectory(context: Context): String? {
        val absolutePath = context.getExternalFilesDir(null)?.absolutePath
        absolutePath ?: return null
        val index = absolutePath.indexOf(Const.ANDROID_DIR).inc()
        return when (index) {
            0 -> null
            else -> absolutePath.substring(0, index)
        }
    }

    fun String.endingDot(): String = "${this}."

    fun InputStream.writeTo(stream: OutputStream): Boolean {
        var remaining = available()
        val bytes = ByteArray(1024)
        while (remaining > 0) {
            val length = min(bytes.size, remaining)
            val read = read(bytes, 0, length)
            if (read < 0) break
            stream.write(bytes, 0, length)
            remaining -= read
        }
        return remaining == 0
    }
}