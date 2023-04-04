package app.atomofiron.searchboxapp.screens.viewer

import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.finder.FinderTask
import app.atomofiron.searchboxapp.model.textviewer.SearchTask
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsState
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsStateDelegate
import app.atomofiron.searchboxapp.utils.toInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class TextViewerViewState(
    private val scope: CoroutineScope,
    private val session: TextViewerSession,
    preferenceStore: PreferenceStore,
) : FinderItemsState by FinderItemsStateDelegate(isLocal = true) {

    data class Status(
        val loading: Boolean = false,
        val count: Int = 0,
        val countMax: Int = 0,
    )
    @JvmInline
    value class MatchCursor(val value: Long = -1) {
        val isEmpty get() = value < 0
        val lineIndex get() = if (isEmpty) -1 else value.shr(32).toInt()
        val lineMatchIndex get() = if (isEmpty) -1 else value.toInt()

        constructor(lineIndex: Int, matchIndex: Int = 0) : this(lineIndex.toLong().shl(32) + matchIndex.toLong())

        fun copy(lineIndex: Int = this.lineIndex, matchIndex: Int = this.lineMatchIndex): MatchCursor {
            return MatchCursor(lineIndex.toLong().shl(32) + matchIndex.toLong())
        }
    }

    val insertInQuery = ChannelFlow<String>()

    val status = MutableStateFlow(Status())
    /** line index -> line match index */
    val matchesCursor = MutableStateFlow(MatchCursor())

    val composition = preferenceStore.explorerItemComposition.value
    val item = session.item
    val tasks = session.tasks
    val textLines = session.textLines
    val currentTask = MutableStateFlow<SearchTask.Done?>(null)

    /** @return value >= 0 если нужно подгрузить файл. */
    fun changeCursor(increment: Boolean): Int {
        val none = -1
        val cursor = matchesCursor.value
        val task = currentTask.value ?: return none
        val matchesMap = currentTask.value?.matchesMap ?: return none
        if (cursor.isEmpty) {
            matchesCursor.value = MatchCursor(lineIndex = task.indexes.first(), matchIndex = 0)
            status.value = status.value.copy(count = 1)
            return none
        }
        var lineIndex = cursor.lineIndex
        var matchIndex = cursor.lineMatchIndex

        if (increment) {
            matchIndex++
            val matches = matchesMap[lineIndex] ?: return none
            if (matchIndex == matches.size) {
                val index = task.indexes.indexOf(lineIndex)
                if (index == task.indexes.lastIndex) return none
                lineIndex = task.indexes[index.inc()]
                matchIndex = 0
            }
        } else {
            matchIndex--
            if (matchIndex < 0) {
                val index = task.indexes.indexOf(lineIndex)
                if (index <= 0) return none
                lineIndex = task.indexes[index.dec()]
                matchIndex = matchesMap[lineIndex]!!.lastIndex
            }
        }
        if (lineIndex > textLines.value.lastIndex) {
            return lineIndex
        }
        matchesCursor.value = MatchCursor(lineIndex, matchIndex)
        status.run {
            value = value.copy(count = value.count + increment.toInt())
        }
        return none
    }

    fun setTasks(tasks: List<FinderTask>) {
        val items = tasks.map { FinderStateItem.ProgressItem(it) }
        progressItems.clear()
        progressItems.addAll(items)
        updateState()
    }

    fun sendInsertInQuery(value: String) {
        insertInQuery[scope] = value
    }

    fun setLoading(loading: Boolean) {
        status.run {
            value = value.copy(loading = loading)
        }
    }
}