package app.atomofiron.searchboxapp.screens.viewer

import android.content.Context
import android.content.Intent
import app.atomofiron.common.arch.BasePresenter
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.injectable.channel.TextViewerChannel
import app.atomofiron.searchboxapp.injectable.interactor.TextViewerInteractor
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.MutableXFile
import app.atomofiron.searchboxapp.model.finder.FinderQueryParams
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.viewer.presenter.SearchAdapterPresenterDelegate
import app.atomofiron.searchboxapp.screens.viewer.recycler.TextViewerAdapter

class TextViewerPresenter(
        viewModel: TextViewerViewModel,
        router: TextViewerRouter,
        searchAdapterPresenterDelegate: SearchAdapterPresenterDelegate,
        private val interactor: TextViewerInteractor,
        preferenceStore: PreferenceStore,
        textViewerChannel: TextViewerChannel
) : BasePresenter<TextViewerViewModel, TextViewerRouter>(viewModel, router),
        TextViewerAdapter.TextViewerListener,
        FinderAdapterOutput by searchAdapterPresenterDelegate
{
    private var lineIndexMatchesMap: Map<Int, List<TextLineMatch>> = HashMap()
    private var matchesCount: Int? = null

    init {
        textViewerChannel.textFromFile.collect(scope) {
            scope.launch {
                viewModel.textLines.value = it
            }
        }
        textViewerChannel.lineIndexMatches.collect(scope) {
            viewModel.lineIndexMatches = it
        }
        textViewerChannel.lineIndexMatchesMap.collect(scope) {
            scope.launch {
                lineIndexMatchesMap = it
                viewModel.matchesMap.value = it
            }
        }
        textViewerChannel.matchesCount.collect(scope) {
            scope.launch {
                matchesCount = it
                viewModel.matchesCounter.value = matchesCount?.toLong()
                viewModel.matchesCursor.value = null
            }
        }
        textViewerChannel.textFromFileLoading.collect(scope) {
            scope.launch {
                viewModel.loading.value = it
            }
        }
        textViewerChannel.tasks.collect(scope) {
            scope.launch {
                viewModel.setTasks(it)
            }
        }
        viewModel.composition = preferenceStore.explorerItemComposition.entity
    }

    override fun onCreate(context: Context, intent: Intent) {
        val path = intent.getStringExtra(TextViewerFragment.KEY_PATH)!!
        val query = intent.getStringExtra(TextViewerFragment.KEY_QUERY)
        val useRegex = intent.getBooleanExtra(TextViewerFragment.KEY_USE_REGEX, false)
        val ignoreCase = intent.getBooleanExtra(TextViewerFragment.KEY_IGNORE_CASE, false)
        val params = query?.let { FinderQueryParams(it, useRegex, ignoreCase) }
        val xFile = MutableXFile.byPath(path)
        interactor.loadFile(xFile, params) {
            viewModel.xFile = xFile
        }
    }

    override fun onLineVisible(index: Int) = interactor.onLineVisible(index)

    fun onPreviousClick() = viewModel.changeCursor(increment = false)

    fun onNextClick() {
        val success = viewModel.changeCursor(increment = true)
        if (!success) {
            interactor.loadFileUpToLine(viewModel.currentLineIndexCursor) {
                scope.launch {
                    viewModel.changeCursor(increment = true)
                }
            }
            viewModel.loading.value = true
        }
    }
}