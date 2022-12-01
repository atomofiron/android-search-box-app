package app.atomofiron.searchboxapp.injectable.service

import app.atomofiron.common.util.flow.emitLast
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import app.atomofiron.searchboxapp.injectable.channel.TextViewerChannel
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.logE
import app.atomofiron.searchboxapp.model.CacheConfig
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.FinderQueryParams
import app.atomofiron.searchboxapp.model.finder.MutableFinderTask
import app.atomofiron.searchboxapp.model.textviewer.LineIndexMatches
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.update
import app.atomofiron.searchboxapp.utils.Shell
import app.atomofiron.searchboxapp.utils.escapeQuotes

class TextViewerService(
    private val textViewerChannel: TextViewerChannel,
    private val preferenceStore: PreferenceStore,
) {
    companion object {
        private const val UNKNOWN = -1L
    }
    private class Match(
        val byteOffset: Long,
        val textLength: Int
    )
    private lateinit var matchesLineIndexes: MutableList<Int>
    /** line index -> matches byteOffset-textLength */
    private lateinit var matchesMap: MutableMap<Int, MutableList<Match>>
    /** line index -> matches start-end */
    private lateinit var textLineMatches: MutableList<LineIndexMatches>
    /** line index -> matches start-end */
    private lateinit var textLineMatchesMap: MutableMap<Int, List<TextLineMatch>>
    private lateinit var lines: MutableList<TextLine>
    private val tasks: MutableList<MutableFinderTask> = ArrayList()
    private val useSu: Boolean get() = preferenceStore.useSu.value
    private var lock = false
    private val mutex = Mutex()
    private var primaryParams: FinderQueryParams? = null
    private var currentTask: MutableFinderTask? = null

    private lateinit var path: String
    private lateinit var item: Node
    private var textOffset = 0L
    private var isEndReached = false
    private var fileSize = UNKNOWN

    suspend fun showTask(task: MutableFinderTask) {
        if (task == currentTask) {
            return
        }
        loadFile(task)
    }

    fun removeTask(task: MutableFinderTask) {
        if (task == currentTask) {
            textViewerChannel.lineIndexMatches.value = listOf()
            textViewerChannel.lineIndexMatchesMap.value = hashMapOf()
            textViewerChannel.matchesCount.value = null
        }
        tasks.remove(task)
        textViewerChannel.tasks.value = tasks
    }

    suspend fun primarySearch(item: Node, params: FinderQueryParams?) {
        this.item = item
        path = item.path
        primaryParams = params
        when (params) {
            null -> loadFile(params)
            else -> {
                val task = addTask(isPrimary = true, params = params)
                loadFile(task)
            }
        }
    }

    suspend fun secondarySearch(params: FinderQueryParams) {
        val task = addTask(isPrimary = false, params = params)
        loadFile(task)
    }

    private fun addTask(isPrimary: Boolean, params: FinderQueryParams): MutableFinderTask {
        val task = MutableFinderTask.local(isRemovable = !isPrimary, params = params)
        tasks.add(task)
        textViewerChannel.tasks.value = tasks
        return task
    }

    private suspend fun loadFile(task: MutableFinderTask? = null) {
        textOffset = 0
        isEndReached = false
        lines = ArrayList()
        matchesMap = HashMap()
        textLineMatches = ArrayList()
        textLineMatchesMap = HashMap()
        matchesLineIndexes = ArrayList()

        textViewerChannel.textFromFile.value = lines
        textViewerChannel.lineIndexMatches.value = textLineMatches
        textViewerChannel.lineIndexMatchesMap.value = textLineMatchesMap
        textViewerChannel.matchesCount.value = null

        fileSize = getFileSize()
        item.update(CacheConfig(useSu))
        currentTask = task

        when (task?.params) {
            null -> loadUpToLine(0)
            else -> {
                val matchesCount = searchInFile(task.params)
                textViewerChannel.matchesCount.value = matchesCount

                task.count = matchesCount
                task.isDone = true
                textViewerChannel.tasks.value = tasks

                loadUpToLine(0)
            }
        }
    }

    suspend fun onLineVisible(index: Int) = loadUpToLine(index)

    suspend fun loadFileUpToLine(prevIndex: Int?) {
        val indexOfNext = when (prevIndex) {
            null -> 0
            else -> matchesLineIndexes.indexOf(prevIndex).inc()
        }
        val nextLineIndex = matchesLineIndexes[indexOfNext]
        loadUpToLine(nextLineIndex)
    }

    private suspend fun loadUpToLine(index: Int) {
        if (!isEndReached && index > lines.size - Const.TEXT_FILE_PAGINATION_STEP_OFFSET) {
            mutex.withLock(lock) {
                if (lock) {
                    return
                }
                lock = true
            }

            textViewerChannel.textFromFileLoading.value = true
            val step = index - lines.size + Const.TEXT_FILE_PAGINATION_STEP
            loadNext(step)
            textViewerChannel.lineIndexMatches.emitLast()
            textViewerChannel.textFromFile.value = ArrayList(lines)
            textViewerChannel.textFromFileLoading.value = false

            lock = false
        }
    }

    private fun loadNext(step: Int) {
        val fileSize = getFileSize()
        if (this.fileSize != fileSize) {
            logE("File size was changed! $path")
            isEndReached = true
            return
        }
        val offset = lines.size
        val cmd = Shell[Shell.HEAD_TAIL].format(path, offset + step, step)
        val output = Shell.exec(cmd, useSu) { line ->
            val index = lines.size
            val match = matchesMap[index]
            match?.map {
                val byteOffsetInLine = (it.byteOffset - textOffset).toInt()
                val bytes = line.toByteArray(Charsets.UTF_8).copyOf(byteOffsetInLine)
                val offsetInLine = String(bytes, Charsets.UTF_8).length
                TextLineMatch(offsetInLine, offsetInLine + it.textLength)
            }?.let {
                val lineMatches = LineIndexMatches(index, it)
                textLineMatches.add(lineMatches)
                textLineMatchesMap[index] = it
            }
            val textLine = TextLine(line)
            lines.add(textLine)
            textOffset += line.toByteArray(Charsets.UTF_8).size.inc()
        }
        if (!output.success) {
            logE("loadNext !success, error: ${output.error}")
        }

        isEndReached = lines.size - offset < step
        isEndReached = isEndReached || textOffset >= fileSize
    }

    private fun getFileSize(): Long {
        val lsLong = Shell[Shell.LS_LOG].format(path)
        val output = Shell.exec(lsLong, useSu)
        return when {
            output.success -> output.output.split(Const.SPACE)[2].toLong()
            else -> {
                logE("getFileSize !success, error: ${output.error}")
                UNKNOWN
            }
        }
    }

    private fun searchInFile(params: FinderQueryParams): Int {
        val template = when {
            params.useRegex && params.ignoreCase -> Shell.GREP_BONS_IE
            params.useRegex -> Shell.GREP_BONS_E
            params.ignoreCase -> Shell.GREP_BONS_I
            else -> Shell.GREP_BONS
        }
        var count = 0
        val cmd = Shell[template].format(params.query.escapeQuotes(), path)
        val output = Shell.exec(cmd, useSu) {
            val lineByteOffset = it.split(':')
            val lineIndex = lineByteOffset[0].toInt().dec()
            val byteOffset = lineByteOffset[1].toLong()
            val text = lineByteOffset[2]
            val match = Match(byteOffset, text.length)
            var list = matchesMap[lineIndex]
            if (list == null) {
                list = ArrayList()
                matchesMap[lineIndex] = list
                matchesLineIndexes.add(lineIndex)
            }
            list.add(match)
            count++
        }
        if (!output.success) {
            logE("searchInFile !success, error: ${output.error}")
        }
        return count
    }
}