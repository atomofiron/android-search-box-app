package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.common.util.KObservable
import app.atomofiron.searchboxapp.model.finder.FinderTask
import app.atomofiron.searchboxapp.model.textviewer.LineIndexMatches
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch

class TextViewerChannel {
    val textFromFile = KObservable<List<TextLine>>()
    val textFromFileLoading = KObservable<Boolean>()
    val lineIndexMatches = KObservable<List<LineIndexMatches>>()
    val lineIndexMatchesMap = KObservable<Map<Int, List<TextLineMatch>>>()
    val matchesCount = KObservable<Int?>()
    val tasks = KObservable<List<FinderTask>>()
}