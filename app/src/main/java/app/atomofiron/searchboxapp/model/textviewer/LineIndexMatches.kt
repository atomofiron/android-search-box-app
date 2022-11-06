package app.atomofiron.searchboxapp.model.textviewer

data class LineIndexMatches(
    val lineIndex: Int,
    val lineMatches: List<TextLineMatch>,
)
