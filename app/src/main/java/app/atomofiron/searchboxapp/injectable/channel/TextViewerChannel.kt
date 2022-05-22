package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.common.util.flow.sharedFlow
import app.atomofiron.searchboxapp.model.finder.FinderTask
import app.atomofiron.searchboxapp.model.textviewer.LineIndexMatches
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch

class TextViewerChannel {
    val textFromFile = sharedFlow<List<TextLine>>()
    val textFromFileLoading = sharedFlow<Boolean>()
    val lineIndexMatches = sharedFlow<List<LineIndexMatches>>()
    val lineIndexMatchesMap = sharedFlow<Map<Int, List<TextLineMatch>>>()
    val matchesCount = sharedFlow<Int?>()
    val tasks = sharedFlow<List<FinderTask>>()
}