package ru.atomofiron.regextool.injectable.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.atomofiron.regextool.injectable.channel.TextViewerChannel
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.model.finder.FinderQueryParams
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.utils.Shell

class TextViewerService(
        private val textViewerChannel: TextViewerChannel,
        private val preferenceStore: PreferenceStore
) {
    private val lines = ArrayList<String>()
    private val useSu: Boolean get() = preferenceStore.useSu.value
    private var lock = false
    private val mutex = Mutex()

    private lateinit var path: String

    init {
        textViewerChannel.textFromFile.setAndNotify(lines)
    }

    suspend fun loadFile(path: String, params: FinderQueryParams?) {
        this.path = path
        when (params) {
            null -> onLineVisible(0)
            else -> searchInFile(params)
        }
    }

    suspend fun onLineVisible(index: Int) {
        if (index > lines.size - Const.TEXT_FILE_PAGINATION_STEP_OFFSET) {
            mutex.withLock(lock) {
                if (lock) {
                    return
                }
                lock = true
            }

            textViewerChannel.textFromFileLoading.setAndNotify(true)
            loadNext()
            textViewerChannel.textFromFileLoading.setAndNotify(false)

            lock = false
        }
    }

    private fun loadNext() {
        val offset = lines.size
        val cmd = Shell[Shell.HEAD_TAIL].format(path, offset + Const.TEXT_FILE_PAGINATION_STEP, Const.TEXT_FILE_PAGINATION_STEP)
        Shell.exec(cmd, useSu) { line ->
            lines.add(line)
        }
        textViewerChannel.textFromFile.setAndNotify(ArrayList(lines))
    }

    private suspend fun searchInFile(params: FinderQueryParams) {
        val template = when {
            params.useRegex && params.ignoreCase -> Shell.GREP_IE
            params.useRegex -> Shell.GREP_E
            params.ignoreCase -> Shell.GREP_I
            else -> Shell.GREP
        }
        val cmd = Shell[template].format(params.query, path)
        Shell.exec(cmd, useSu) {
            // todo next "line:byte:match"
        }
        onLineVisible(0)
    }
}