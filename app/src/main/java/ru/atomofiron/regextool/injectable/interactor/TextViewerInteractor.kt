package ru.atomofiron.regextool.injectable.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.injectable.service.TextViewerService
import ru.atomofiron.regextool.model.finder.FinderQueryParams

class TextViewerInteractor(
        private val scope: CoroutineScope,
        private val textViewerService: TextViewerService
) {
    fun loadFile(path: String, params: FinderQueryParams?) {
        scope.launch {
            textViewerService.loadFile(path, params)
        }
    }

    fun onLineVisible(index: Int) {
        scope.launch {
            textViewerService.onLineVisible(index)
        }
    }
}