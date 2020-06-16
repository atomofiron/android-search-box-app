package ru.atomofiron.regextool.injectable.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.atomofiron.regextool.injectable.channel.TextViewerChannel
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.logE
import ru.atomofiron.regextool.model.explorer.MutableXFile
import ru.atomofiron.regextool.model.finder.FinderQueryParams
import ru.atomofiron.regextool.model.finder.MutableFinderTask
import ru.atomofiron.regextool.model.textviewer.LineIndexMatches
import ru.atomofiron.regextool.model.textviewer.TextLine
import ru.atomofiron.regextool.model.textviewer.TextLineMatch
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Shell

class TextViewerService(
        private val textViewerChannel: TextViewerChannel,
        private val preferenceStore: PreferenceStore
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

    private lateinit var path: String
    private lateinit var xFile: MutableXFile
    private var textOffset = 0L
    private var isEndReached = false
    private var fileSize = UNKNOWN

    suspend fun primarySearch(xFile: MutableXFile, params: FinderQueryParams?) {
        this.xFile = xFile
        path = xFile.completedPath
        primaryParams = params
        when {
            params != null -> {
                val task = addTask(isPrimary = true)
                loadFile(params, task)
            }
            else -> loadFile(params)
        }
    }

    suspend fun secondarySearch(params: FinderQueryParams) {
        val task = addTask(isPrimary = false)
        loadFile(params, task)
    }

    private fun addTask(isPrimary: Boolean): MutableFinderTask {
        val task = MutableFinderTask.secondary(isRemovable = !isPrimary)
        tasks.add(task)
        textViewerChannel.tasks.setAndNotify(tasks)
        return task
    }

    private suspend fun loadFileWithPrimaryParams() = loadFile(primaryParams)

    private suspend fun loadFile(params: FinderQueryParams?, task: MutableFinderTask? = null) {
        lines = ArrayList()
        matchesMap = HashMap()
        textLineMatches = ArrayList()
        textLineMatchesMap = HashMap()
        matchesLineIndexes = ArrayList()

        textViewerChannel.textFromFile.setAndNotify(lines)
        textViewerChannel.lineIndexMatches.setAndNotify(textLineMatches)
        textViewerChannel.lineIndexMatchesMap.setAndNotify(textLineMatchesMap)
        textViewerChannel.matchesCount.setAndNotify(null)

        fileSize = getFileSize()
        xFile.updateCache(useSu)

        when (params) {
            null -> loadUpToLine(0)
            else -> {
                val matchesCount = searchInFile(params)
                textViewerChannel.matchesCount.setAndNotify(matchesCount)

                task!!.count = matchesCount
                textViewerChannel.tasks.setAndNotify(tasks)

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

            textViewerChannel.textFromFileLoading.setAndNotify(true)
            val step = index - lines.size + Const.TEXT_FILE_PAGINATION_STEP
            loadNext(step)
            textViewerChannel.lineIndexMatches.justNotify()
            textViewerChannel.textFromFile.setAndNotify(ArrayList(lines))
            textViewerChannel.textFromFileLoading.setAndNotify(false)

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
        Shell.exec(cmd, useSu) { line ->
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

        isEndReached = lines.size - offset < step
        isEndReached = isEndReached || textOffset >= fileSize
    }

    private fun getFileSize(): Long {
        val lsLong = Shell[Shell.LS_LOG].format(path)
        val output = Shell.exec(lsLong, useSu)
        return when {
            output.success -> output.output.split(Const.SPACE)[2].toLong()
            else -> UNKNOWN
        }
    }

    private fun searchInFile(params: FinderQueryParams): Int {
        val template = when {
            params.useRegex && params.ignoreCase -> Shell.GREP_IE
            params.useRegex -> Shell.GREP_E
            params.ignoreCase -> Shell.GREP_I
            else -> Shell.GREP
        }
        var count = 0
        val cmd = Shell[template].format(params.query, path)
        Shell.exec(cmd, useSu) {
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
        return count
    }
}