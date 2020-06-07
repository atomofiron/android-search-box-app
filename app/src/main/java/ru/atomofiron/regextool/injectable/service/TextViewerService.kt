package ru.atomofiron.regextool.injectable.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.atomofiron.regextool.injectable.channel.TextViewerChannel
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.logE
import ru.atomofiron.regextool.model.finder.FinderQueryParams
import ru.atomofiron.regextool.model.textviewer.LineIndexMatches
import ru.atomofiron.regextool.model.textviewer.TextLine
import ru.atomofiron.regextool.model.textviewer.TextLineMatch
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Shell
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
    private val matches = HashMap<Int, MutableList<Match>>()
    private val textLineMatches = ArrayList<LineIndexMatches>()
    private val textLineMatchesMap = HashMap<Int, List<TextLineMatch>>()
    private val lines = ArrayList<TextLine>()
    private val useSu: Boolean get() = preferenceStore.useSu.value
    private var lock = false
    private val mutex = Mutex()

    private lateinit var path: String
    private var textOffset = 0L
    private var isEndReached = false
    private var fileSize = UNKNOWN

    init {
        textViewerChannel.textFromFile.setAndNotify(lines)
        textViewerChannel.globalMatches.setAndNotify(textLineMatches)
        textViewerChannel.globalMatchesMap.setAndNotify(textLineMatchesMap)
        textViewerChannel.globalMatchesCount.setAndNotify(null)
    }

    suspend fun loadFile(path: String, params: FinderQueryParams?) {
        this.path = path
        fileSize = getFileSize()
        when (params) {
            null -> onLineVisible(0)
            else -> {
                val matchesCount = searchInFile(params)
                textViewerChannel.globalMatchesCount.setAndNotify(matchesCount)
                onLineVisible(0)
            }
        }
    }

    suspend fun onLineVisible(index: Int) {
        if (!isEndReached && index > lines.size - Const.TEXT_FILE_PAGINATION_STEP_OFFSET) {
            mutex.withLock(lock) {
                if (lock) {
                    return
                }
                lock = true
            }

            textViewerChannel.textFromFileLoading.setAndNotify(true)
            loadNext()
            textViewerChannel.globalMatches.justNotify()
            textViewerChannel.textFromFile.setAndNotify(ArrayList(lines))
            textViewerChannel.textFromFileLoading.setAndNotify(false)

            lock = false
        }
    }

    private fun loadNext() {
        val fileSize = getFileSize()
        if (this.fileSize != fileSize) {
            logE("File size was changed! $path")
            isEndReached = true
            return
        }
        val offset = lines.size
        val cmd = Shell[Shell.HEAD_TAIL].format(path, offset + Const.TEXT_FILE_PAGINATION_STEP, Const.TEXT_FILE_PAGINATION_STEP)
        Shell.exec(cmd, useSu) { line ->
            val index = lines.size
            val match = matches[index]
            match?.map {
                val byteOffsetInLine = (it.byteOffset - textOffset).toInt()
                val bytes = Arrays.copyOf(line.toByteArray(Charsets.UTF_8), byteOffsetInLine)
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

        isEndReached = lines.size - offset < Const.TEXT_FILE_PAGINATION_STEP
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
            var list = matches[lineIndex]
            if (list == null) {
                list = ArrayList()
                matches[lineIndex] = list
            }
            list.add(match)
            count++
        }
        return count
    }
}