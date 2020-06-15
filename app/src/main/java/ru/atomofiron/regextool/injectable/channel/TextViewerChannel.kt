package ru.atomofiron.regextool.injectable.channel

import app.atomofiron.common.util.KObservable
import ru.atomofiron.regextool.model.finder.FinderTask
import ru.atomofiron.regextool.model.textviewer.LineIndexMatches
import ru.atomofiron.regextool.model.textviewer.TextLine
import ru.atomofiron.regextool.model.textviewer.TextLineMatch

class TextViewerChannel {
    val textFromFile = KObservable<List<TextLine>>()
    val textFromFileLoading = KObservable<Boolean>()
    val lineIndexMatches = KObservable<List<LineIndexMatches>>()
    val lineIndexMatchesMap = KObservable<Map<Int, List<TextLineMatch>>>()
    val matchesCount = KObservable<Int?>()
    val localTasks = KObservable<List<FinderTask>>()
}