package app.atomofiron.searchboxapp.injectable.service

import app.atomofiron.searchboxapp.injectable.store.*
import app.atomofiron.searchboxapp.logE
import app.atomofiron.searchboxapp.model.CacheConfig
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.finder.FinderQueryParams
import app.atomofiron.searchboxapp.model.finder.MutableFinderTask
import app.atomofiron.searchboxapp.model.textviewer.SearchTask
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.update
import app.atomofiron.searchboxapp.utils.Shell
import app.atomofiron.searchboxapp.utils.escapeQuotes
import app.atomofiron.searchboxapp.utils.removeOneIf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TextViewerService(
    private val scope: CoroutineScope,
    private val preferenceStore: PreferenceStore,
    private val textViewerStore: TextViewerStore,
    private val finderStore: FinderStore,
) {

    private val useSu: Boolean get() = preferenceStore.useSu.value

    fun getFileSession(path: String): TextViewerSession {
        val item = Node(path, content = NodeContent.Unknown)
        var session = findSession(item)
        if (session == null) {
            session = TextViewerSession(item)
            textViewerStore.sessions[item.uniqueId] = session
            scope.launch(Dispatchers.IO) { readFile(item) }
        }
        if (!item.isCached) {
            scope.launch(Dispatchers.IO) {
                val config = CacheConfig(useSu, thumbnailSize = 0)
                session.item.value = item.update(config)
            }
        }
        return session
    }

    fun fetchTask(item: Node, taskId: Long) {
        val session = findSession(item) ?: return
        var task = session.tasks.value.find { it.id == taskId }
        if (task != null) return
        val finderTask = finderStore.tasks.find { it.id == taskId } as MutableFinderTask?
        finderTask ?: return logE("Finder task not found for ${item.path}")
        val result = finderTask.results.find { it.item.path == item.path }
        result ?: return logE("Finder task result not found for ${item.path}")
        // todo task = TextViewerTask.Done(taskId, isRemovable = false, )
    }

    /** @return true if success */
    fun readFile(item: Node, targetLineIndex: Int = 0): Boolean {
        val session = findSession(item) ?: return false
        val paginationThreshold = session.textLines.value.size - Const.TEXT_FILE_PAGINATION_STEP_OFFSET
        return when {
            targetLineIndex < paginationThreshold -> false
            else -> session.readNextLines()
        }
    }

    fun closeSession(item: Node) {
        val session = textViewerStore.sessions.remove(item.uniqueId)
        session?.reader?.close()
    }

    fun removeTask(item: Node, taskId: Long) {
        val session = findSession(item) ?: return
        val tasks = session.tasks.value.toMutableList()
        tasks.removeOneIf { it.id == taskId }
        session.tasks.value = tasks
    }

    fun search(item: Node, params: FinderQueryParams) {
        val session = findSession(item) ?: return
        val taskProgress = session.addProgressTask(params)
        val taskDone = session.searchInside(taskProgress)
        session.finishTask(taskDone)
    }

    private fun findSession(item: Node): TextViewerSession? {
        return textViewerStore.sessions[item.uniqueId].also { task ->
            task ?: logE("Session not found for ${item.path}")
        }
    }

    private fun TextViewerSession.readNextLines(): Boolean {
        if (isFullyRead || !mutex.tryLock()) return false
        textLoading.value = true
        val lines = ArrayList<TextLine>(Const.TEXT_FILE_PAGINATION_STEP)
        var byteOffset = textLines.value.lastOrNull()?.run { byteOffset + byteCount.inc() } ?: 0
        while (lines.size < Const.TEXT_FILE_PAGINATION_STEP) {
            when (val stringOrNull = reader.readLine()) {
                null -> {
                    isFullyRead = true
                    break
                }
                else -> {
                    val byteCount = stringOrNull.toByteArray().size
                    lines.add(TextLine(byteOffset, byteCount, stringOrNull))
                    byteOffset += byteCount.inc() // \n
                }
            }
        }
        val textLines = textLines.value.toMutableList()
        textLines.addAll(lines)
        this.textLines.value = textLines
        textLoading.value = false
        mutex.unlock()
        return true
    }

    private fun TextViewerSession.addProgressTask(params: FinderQueryParams): SearchTask.Progress {
        val task = SearchTask.Progress(params)
        tasks.run {
            val tasks = value.toMutableList()
            tasks.add(0, task)
            value = tasks
        }
        return task
    }

    private fun TextViewerSession.searchInside(task: SearchTask.Progress): SearchTask.Ended {
        val params = task.params
        val template = when {
            params.useRegex && params.ignoreCase -> Shell.GREP_BONS_IE
            params.useRegex -> Shell.GREP_BONS_E
            params.ignoreCase -> Shell.GREP_BONS_I
            else -> Shell.GREP_BONS
        }
        var count = 0
        val cmd = Shell[template].format(params.query.escapeQuotes(), item.value.path)
        val lineIndexToMatches = hashMapOf<Int, MutableList<TextLineMatch>>()
        val output = Shell.exec(cmd, useSu) { line ->
            val lineByteOffset = line.split(':')
            val lineIndex = lineByteOffset[0].toInt().dec()
            val byteOffset = lineByteOffset[1].toLong()
            val text = lineByteOffset[2]
            val lineMatch = TextLineMatch(byteOffset, text.length)
            var list = lineIndexToMatches[lineIndex]
            if (list == null) {
                list = mutableListOf()
                lineIndexToMatches[lineIndex] = list
            }
            list.add(lineMatch)
            count++
        }
        return if (output.success || output.code == 1 && output.error.isEmpty()) {
            val indexes = lineIndexToMatches.keys.sorted()
            SearchTask.Done(task.queryId, isRemovable = true, params, count, lineIndexToMatches, indexes)
        } else  {
            logE("searchInFile !success, error: ${output.error}")
            SearchTask.Error(task.queryId, params, output.error)
        }
    }

    private fun TextViewerSession.finishTask(task: SearchTask.Ended) {
        tasks.run {
            val index = value.indexOfFirst { it.queryId == task.queryId }
            if (index < 0) return logE("No Progress task with query ${task.params.query}")
            val tasks = value.toMutableList()
            tasks[index] = task
            value = tasks
        }
    }
}