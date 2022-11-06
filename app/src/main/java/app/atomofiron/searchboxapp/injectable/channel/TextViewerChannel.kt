package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.searchboxapp.model.finder.FinderTask
import app.atomofiron.searchboxapp.model.textviewer.LineIndexMatches
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch
import kotlinx.coroutines.flow.MutableStateFlow

class TextViewerChannel {
    val textFromFile = MutableStateFlow<List<TextLine>>(listOf())
    val textFromFileLoading = MutableStateFlow(true)
    val lineIndexMatches = MutableStateFlow<List<LineIndexMatches>>(listOf())
    val lineIndexMatchesMap = MutableStateFlow<Map<Int, List<TextLineMatch>>>(hashMapOf())
    val matchesCount = MutableStateFlow<Int?>(null)
    val tasks = MutableStateFlow<List<FinderTask>>(listOf())
}