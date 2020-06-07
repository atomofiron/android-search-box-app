package ru.atomofiron.regextool.model.textviewer

open class TextLine(
        val text: String
) {
    class Match(
            val start: Int,
            val end: Int
    )
}