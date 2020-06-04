package ru.atomofiron.regextool.model.textviewer

class TextLine(
        val text: String,
        val matches: List<Pair<Int, Int>>? = null // start, end
)