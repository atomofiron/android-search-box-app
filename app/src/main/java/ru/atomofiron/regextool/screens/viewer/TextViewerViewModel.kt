package ru.atomofiron.regextool.screens.viewer

import androidx.lifecycle.MutableLiveData
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.LateinitLiveData
import ru.atomofiron.regextool.di.DaggerInjector
import ru.atomofiron.regextool.model.textviewer.LineIndexMatches
import ru.atomofiron.regextool.model.textviewer.TextLine
import ru.atomofiron.regextool.model.textviewer.TextLineMatch

class TextViewerViewModel : BaseViewModel<TextViewerComponent, TextViewerFragment>() {
    companion object {
        const val UNDEFINED = -1
    }
    override val component = DaggerTextViewerComponent.builder()
            .bind(this)
            .bind(viewProperty)
            .dependencies(DaggerInjector.appComponent)
            .build()

    override fun inject(view: TextViewerFragment) {
        super.inject(view)
        component.inject(this)
        component.inject(view)
    }

    val textLines = LateinitLiveData<List<TextLine>>()
    /** line index -> line matches */
    val matchesMap = LateinitLiveData<Map<Int, List<TextLineMatch>>>()
    /** match counter -> matches quantity */
    val matchesCounter = MutableLiveData<Long?>(null)
    /** line index -> line match index */
    val matchesCursor = MutableLiveData<Long?>(null)
    val loading = LateinitLiveData(true)

    private var matchesIndex = -1
    var globalMatches: List<LineIndexMatches> = ArrayList()
    var localMatches: List<LineIndexMatches>? = null
    /** line index -> line matches */
    /*private*/ val matches: List<LineIndexMatches> get() = when (localMatches) {
        null -> globalMatches
        else -> localMatches!!
    }

    val currentLineIndexCursor: Int? get() = matchesCursor.value?.shr(32)?.toInt()

    /** @return true если есть на что переключаться, иначе нужно догрузить файл. */
    fun changeCursor(increment: Boolean): Boolean {
        val cursor = matchesCursor.value
        val matches = matches
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
                    increment -> {
                        matchesCursor.value = lineIndex.toLong().shl(32) + matchIndex.inc().toLong()
                    }
                    !increment && matchIndex == 0 -> {
                        val lineIndexMatches = matches[--matchesIndex]
                        matchIndex = lineIndexMatches.lineMatches.size.dec()
                        matchesCursor.value = lineIndexMatches.lineIndex.toLong().shl(32) + matchIndex.toLong()
                    }
                    !increment -> {
                        matchesCursor.value = lineIndex.toLong().shl(32) + matchIndex.dec().toLong()
                    }
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
}