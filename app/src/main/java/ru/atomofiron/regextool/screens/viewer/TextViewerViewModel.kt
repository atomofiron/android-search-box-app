package ru.atomofiron.regextool.screens.viewer

import androidx.lifecycle.MutableLiveData
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.LateinitLiveData
import ru.atomofiron.regextool.di.DaggerInjector
import ru.atomofiron.regextool.model.textviewer.LineIndexMatches
import ru.atomofiron.regextool.model.textviewer.TextLine
import ru.atomofiron.regextool.model.textviewer.TextLineMatch

class TextViewerViewModel : BaseViewModel<TextViewerComponent, TextViewerFragment>() {
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
    val matchesMap = LateinitLiveData<Map<Int, List<TextLineMatch>>>()
    val matchesCounter = MutableLiveData<Long?>(null)
    val matchesCursor = MutableLiveData<Long?>(null)
    val loading = LateinitLiveData(true)

    private var matchesIndex = -1
    var globalMatches: List<LineIndexMatches> = ArrayList()
    var localMatches: List<LineIndexMatches>? = null
    private val matches: List<LineIndexMatches> get() = when (localMatches) {
        null -> globalMatches
        else -> localMatches!!
    }

    fun changeCursor(increment: Boolean) {
        val cursor = matchesCursor.value
        val matches = matches
        when (cursor) {
            null -> {
                if (matches.isEmpty()) {
                    // todo next
                    loading.value = true
                    return
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
                            // todo next
                            loading.value = true
                            return
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
    }
}