package app.atomofiron.searchboxapp.utils

import android.content.Context
import app.atomofiron.searchboxapp.model.explorer.MediaDirectories

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

    fun Context.getMediaDirectories(): MediaDirectories {
        val storage = getExternalStorageDirectory(this) ?: Const.ROOT
        return MediaDirectories(storage)
    }
}