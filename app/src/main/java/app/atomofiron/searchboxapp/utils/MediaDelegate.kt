package app.atomofiron.searchboxapp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import app.atomofiron.searchboxapp.model.CacheConfig
import app.atomofiron.searchboxapp.model.explorer.Node


object MediaDelegate {

    fun Node.getThumbnail(config: CacheConfig): Bitmap {
        val picture = BitmapFactory.decodeFile(path)
        val size = config.previewSize
        val width = if (picture.width < picture.height) size else picture.width * size / picture.height
        val height = if (picture.height < picture.height) size else picture.height * size / picture.width
        return ThumbnailUtils.extractThumbnail(picture, width, height)
    }
}