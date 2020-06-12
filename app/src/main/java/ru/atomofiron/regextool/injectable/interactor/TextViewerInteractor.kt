package ru.atomofiron.regextool.injectable.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.injectable.service.TextViewerService
import ru.atomofiron.regextool.model.explorer.MutableXFile
import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.model.finder.FinderQueryParams

class TextViewerInteractor(
        private val scope: CoroutineScope,
        private val textViewerService: TextViewerService
) {
    fun loadFile(xFile: MutableXFile, params: FinderQueryParams?, callback: () -> Unit) {
        scope.launch {
            textViewerService.loadFile(xFile, params)
            callback()
        }
    }

    fun onLineVisible(index: Int) {
        scope.launch {
            textViewerService.onLineVisible(index)
        }
    }

    fun loadFileUpToLine(prevIndex: Int?, callback: () -> Unit) {
        scope.launch {
            textViewerService.loadFileUpToLine(prevIndex)
            callback()
        }
    }
}