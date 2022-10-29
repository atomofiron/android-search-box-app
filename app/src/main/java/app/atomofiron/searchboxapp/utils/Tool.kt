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
        val storage = getExternalStorageDirectory(this)
        return MediaDirectories(
            pathAndroid = "${storage}Android/",
            pathCamera = "${storage}DCIM/",
            pathDownload = "${storage}Download/",
            pathMovies = "${storage}Movies/",
            pathMusic = "${storage}Music/",
            pathPictures = "${storage}Pictures/",
        )
    }
}