package ru.atomofiron.regextool.injectable.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.atomofiron.regextool.injectable.channel.TextViewerChannel
import ru.atomofiron.regextool.injectable.store.PreferenceStore
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

    suspend fun loadFile(path: String) {
        this.path = path
        onLineVisible(0)
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
        val cmd = Shell.HEAD_TAIL.format(path, offset + Const.TEXT_FILE_PAGINATION_STEP, Const.TEXT_FILE_PAGINATION_STEP)
        Shell.exec(cmd, useSu) { line ->
            lines.add(line)
        }
        textViewerChannel.textFromFile.setAndNotify(ArrayList(lines))
    }
}