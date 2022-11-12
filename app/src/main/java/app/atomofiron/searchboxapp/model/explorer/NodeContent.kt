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
        data class Movie(val duration: Int = 0, val preview: Bitmap? = null) : File()
        data class Music(val duration: Int = 0, val cover: Bitmap? = null) : File()
        sealed class Picture(val thumbnail: Bitmap? = null) : File() {
            class Png(thumbnail: Bitmap? = null) : Picture(thumbnail)
            class Jpeg(thumbnail: Bitmap? = null) : Picture(thumbnail)
            class Webp(thumbnail: Bitmap? = null) : Picture(thumbnail)
        }
        data class Apk(
            val icon: Bitmap? = null,
            val versionName: String = "",
            val versionCode: Int = 0,
            val children: List<Node>? = null,
        ) : File()
        sealed class Archive(val children: List<Node>? = null) : File() {
            class Zip(children: List<Node>? = null) : Archive(children)
            class Bzip2(children: List<Node>? = null) : Archive(children)
            class Gz(children: List<Node>? = null) : Archive(children)
            class Tar(children: List<Node>? = null) : Archive(children)
            class Rar(children: List<Node>? = null) : Archive(children)
        }
        object Text : File()
        object Pdf : File()
        object DataImage : File()
        object Other : File()
        object Unknown : File()
    }
}
