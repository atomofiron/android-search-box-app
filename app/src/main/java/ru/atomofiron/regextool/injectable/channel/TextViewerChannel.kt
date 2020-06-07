package ru.atomofiron.regextool.injectable.channel

import app.atomofiron.common.util.KObservable
import ru.atomofiron.regextool.model.textviewer.TextLine
import ru.atomofiron.regextool.model.textviewer.TextLineMatch

class TextViewerChannel {
    val textFromFile = KObservable<List<TextLineMatch>>()
    val textFromFileLoading = KObservable<Boolean>()
    val localMatches = KObservable<List<List<TextLine.Match>>>()
}