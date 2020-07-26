package app.atomofiron.searchboxapp.injectable.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.injectable.service.TextViewerService
import app.atomofiron.searchboxapp.model.explorer.MutableXFile
import app.atomofiron.searchboxapp.model.finder.FinderQueryParams
import app.atomofiron.searchboxapp.model.finder.FinderTask
import app.atomofiron.searchboxapp.model.finder.MutableFinderTask

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