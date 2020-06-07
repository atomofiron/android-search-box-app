package ru.atomofiron.regextool.model.textviewer

class TextLineMatch(
        text: String,
        val matches: List<Match>?
) : TextLine(text)