package app.atomofiron.searchboxapp.model.explorer

class MediaDirectories private constructor(
    private val pathAndroid: String,
    private val pathCamera: String,
    private val pathDownload: String,
    private val pathMovies: String,
    private val pathMusic: String,
    private val pathPictures: String,
) {
    constructor(storage: String) : this(
        pathAndroid = "${storage}Android/",
        pathCamera = "${storage}DCIM/",
        pathDownload = "${storage}Download/",
        pathMovies = "${storage}Movies/",
        pathMusic = "${storage}Music/",
        pathPictures = "${storage}Pictures/",
    )

    fun getMediaType(path: String): DirectoryMediaType = when (path) {
        pathAndroid -> DirectoryMediaType.Android
        pathCamera -> DirectoryMediaType.Camera
        pathDownload -> DirectoryMediaType.Download
        pathMovies -> DirectoryMediaType.Movies
        pathMusic -> DirectoryMediaType.Music
        pathPictures -> DirectoryMediaType.Pictures
        else -> DirectoryMediaType.None
    }
}

enum class DirectoryMediaType {
    None,
    Android,
    Camera,
    Download,
    Movies,
    Music,
    Pictures
}
