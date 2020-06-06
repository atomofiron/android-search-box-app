package ru.atomofiron.regextool.model.textviewer

class TextLine(
        val text: String,
        val matches: List<Match>? = null
) {
    class Match(
            val start: Int,
            val end: Int
    )
}