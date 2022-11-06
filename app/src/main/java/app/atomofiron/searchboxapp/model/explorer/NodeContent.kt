package app.atomofiron.searchboxapp.model.explorer

import android.graphics.Bitmap

sealed class NodeContent {

    object Unknown : NodeContent()
    object Link : NodeContent()

    data class Directory(val type: Type = Type.Ordinary) : NodeContent() {
        enum class Type {
            Ordinary, Android, Camera, Download, Movies, Music, Pictures,
        }
    }

    sealed class File : NodeContent() {
        data class Archive(val children: List<Node>? = null) : File()
        data class Apk(val icon: Bitmap?, val versionName: String, val versionCode: Int) : File()
        data class Picture(val thumbnail: Bitmap? = null) : File()
        data class Music(val duration: Int, val cover: Bitmap? = null) : File()
        object Text : File()
        object Other : File()
    }
}
