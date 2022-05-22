package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.common.util.flow.dataFlow
import app.atomofiron.searchboxapp.model.finder.FinderTask
import app.atomofiron.searchboxapp.model.textviewer.LineIndexMatches
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch

class TextViewerChannel {
    val textFromFile = dataFlow<List<TextLine>>()
    val textFromFileLoading = dataFlow<Boolean>()
    val lineIndexMatches = dataFlow<List<LineIndexMatches>>()
    val lineIndexMatchesMap = dataFlow<Map<Int, List<TextLineMatch>>>()
    val matchesCount = dataFlow<Int?>()
    val tasks = dataFlow<List<FinderTask>>()
}