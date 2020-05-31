package ru.atomofiron.regextool.injectable.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.injectable.service.TextViewerService

class TextViewerInteractor(
        private val scope: CoroutineScope,
        private val textViewerService: TextViewerService
) {
    fun loadFile(path: String) {
        scope.launch {
            textViewerService.loadFile(path)
        }
    }

    fun onLineVisible(index: Int) {
        scope.launch {
            textViewerService.onLineVisible(index)
        }
    }
}