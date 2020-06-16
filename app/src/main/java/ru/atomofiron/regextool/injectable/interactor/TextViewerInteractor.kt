package ru.atomofiron.regextool.injectable.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.injectable.service.TextViewerService
import ru.atomofiron.regextool.model.explorer.MutableXFile
import ru.atomofiron.regextool.model.finder.FinderQueryParams
import ru.atomofiron.regextool.model.finder.FinderTask
import ru.atomofiron.regextool.model.finder.MutableFinderTask

class TextViewerInteractor(
        private val scope: CoroutineScope,
        private val textViewerService: TextViewerService
) {
    private val context = Dispatchers.IO

    fun loadFile(xFile: MutableXFile, params: FinderQueryParams?, callback: () -> Unit) {
        scope.launch(context) {
            textViewerService.primarySearch(xFile, params)
            callback()
        }
    }

    fun onLineVisible(index: Int) {
        scope.launch(context) {
            textViewerService.onLineVisible(index)
        }
    }

    fun loadFileUpToLine(prevIndex: Int?, callback: () -> Unit) {
        scope.launch(context) {
            textViewerService.loadFileUpToLine(prevIndex)
            callback()
        }
    }

    fun search(query: String, ignoreCase: Boolean, useRegex: Boolean) {
        scope.launch(context) {
            val params = FinderQueryParams(query, useRegex, ignoreCase)
            textViewerService.secondarySearch(params)
        }
    }

    fun showTask(task: FinderTask) {
        scope.launch(context) {
            textViewerService.showTask(task as MutableFinderTask)
        }
    }

    fun removeTask(task: FinderTask) = textViewerService.removeTask(task as MutableFinderTask)
}