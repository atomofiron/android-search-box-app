package app.atomofiron.searchboxapp.screens.viewer

import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.FinderTask
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.model.textviewer.LineIndexMatches
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsState
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsStateDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class TextViewerViewState(
    private val scope: CoroutineScope,
) : FinderItemsState by FinderItemsStateDelegate() {
    companion object {
        const val UNDEFINED = -1
    }

    val insertInQuery = ChannelFlow<String>()

    val textLines = MutableStateFlow<List<TextLine>>(listOf())
    /** line index -> line matches */
    val matchesMap = MutableStateFlow<Map<Int, List<TextLineMatch>>>(hashMapOf())
    /** match counter -> matches quantity */
    val matchesCounter = MutableStateFlow<Long?>(null)
    /** line index -> line match index */
    val matchesCursor = MutableStateFlow<Long?>(null)
    val loading = MutableStateFlow(true)
    lateinit var composition: ExplorerItemComposition
    lateinit var item: Node

    private var matchesIndex = -1
    /** line index -> line matches */
    var lineIndexMatches: List<LineIndexMatches>? = null

    val currentLineIndexCursor: Int? get() = matchesCursor.value?.shr(32)?.toInt()

    /** @return true если есть на что переключаться, иначе нужно догрузить файл. */
    fun changeCursor(increment: Boolean): Boolean {
        val cursor = matchesCursor.value
        val matches = lineIndexMatches!!
        when (cursor) {
            null -> {
                if (matches.isEmpty()) {
                    return false
                }
                val lineIndex = matches.first().lineIndex.toLong()
                matchesCursor.value = lineIndex.shl(32)
                matchesIndex = 0
            }
            else -> {
                val lineIndex = cursor.shr(32).toInt()
                var matchIndex = cursor.toInt()
                val lineMatches = matches[matchesIndex].lineMatches
                when {
                    increment && matchIndex.inc() == lineMatches.size -> {
                        if (matchesIndex.inc() == matches.size) {
                            return false
                        }
                        val lineIndexMatches = matches[++matchesIndex]
                        matchesCursor.value = lineIndexMatches.lineIndex.toLong().shl(32)
                    }
                    increment -> matchesCursor.value = lineIndex.toLong().shl(32) + matchIndex.inc().toLong()
                    matchIndex == 0 -> {
                        val lineIndexMatches = matches[--matchesIndex]
                        matchIndex = lineIndexMatches.lineMatches.size.dec()
                        matchesCursor.value = lineIndexMatches.lineIndex.toLong().shl(32) + matchIndex.toLong()
                    }
                    else -> matchesCursor.value = lineIndex.toLong().shl(32) + matchIndex.dec().toLong()
                }
            }
        }
        val counter = matchesCounter.value!!
        val index = when {
            increment -> counter.shr(32).inc().shl(32)
            else -> counter.shr(32).dec().shl(32)
        }
        val count = counter.toInt().toLong()
        matchesCounter.value = index + count
        return true
    }

    fun setTasks(tasks: List<FinderTask>) {
        val items = tasks.map { FinderStateItem.ProgressItem(it) }
        progressItems.clear()
        progressItems.addAll(items)
        updateState(isLocal = true)
    }

    fun sendInsertInQuery(value: String) {
        insertInQuery[scope] = value
    }
}