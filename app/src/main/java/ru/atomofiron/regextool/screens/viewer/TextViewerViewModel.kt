package ru.atomofiron.regextool.screens.viewer

import androidx.lifecycle.MutableLiveData
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.LateinitLiveData
import ru.atomofiron.regextool.di.DaggerInjector
import ru.atomofiron.regextool.model.textviewer.TextLine

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
    val matches = LateinitLiveData<Map<Int, List<TextLine.Match>?>>()
    val matchesCursor = MutableLiveData<Long>()
    val loading = LateinitLiveData<Boolean>()

    fun changeCursor(increment: Boolean) {
        var cursor = matchesCursor.value
        val matches = matches.value
        when (cursor) {
            null -> {
                val lineIndex = matches.keys.first().toLong()
                cursor = lineIndex.shl(32)
            }
            else -> {
                val lineIndex = cursor.shr(32).toInt()
                val matchIndex = cursor.toInt()
                val match = matches[lineIndex]!!
                when {
                    increment && matchIndex.inc() == match.size -> Unit
                }
            }
        }
    }
}