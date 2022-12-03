package app.atomofiron.searchboxapp.utils

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import app.atomofiron.searchboxapp.model.CacheConfig
import app.atomofiron.searchboxapp.model.explorer.Node


object MediaDelegate {

    fun Node.getThumbnail(config: CacheConfig): Drawable {
        val picture = BitmapFactory.decodeFile(path)
        val size = config.previewSize
        val width = if (picture.width < picture.height) size else picture.width * size / picture.height
        val height = if (picture.height < picture.width) size else picture.height * size / picture.width
        val bitmap = ThumbnailUtils.extractThumbnail(picture, width, height)
        return BitmapDrawable(bitmap)
    }
}