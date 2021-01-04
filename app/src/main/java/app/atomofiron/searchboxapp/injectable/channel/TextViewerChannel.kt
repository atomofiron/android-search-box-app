package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.common.util.flow.DataFlow
import app.atomofiron.searchboxapp.model.finder.FinderTask
import app.atomofiron.searchboxapp.model.textviewer.LineIndexMatches
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch

class TextViewerChannel {
    val textFromFile = DataFlow<List<TextLine>>()
    val textFromFileLoading = DataFlow<Boolean>()
    val lineIndexMatches = DataFlow<List<LineIndexMatches>>()
    val lineIndexMatchesMap = DataFlow<Map<Int, List<TextLineMatch>>>()
    val matchesCount = DataFlow<Int?>()
    val tasks = DataFlow<List<FinderTask>>()
}