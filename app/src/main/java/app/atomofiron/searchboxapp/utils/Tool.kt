package app.atomofiron.searchboxapp.utils

import android.content.Context

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
}