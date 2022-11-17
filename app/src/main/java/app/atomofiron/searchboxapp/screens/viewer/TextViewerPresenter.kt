package app.atomofiron.searchboxapp.screens.viewer

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.collect
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.injectable.channel.TextViewerChannel
import app.atomofiron.searchboxapp.injectable.interactor.TextViewerInteractor
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.finder.FinderQueryParams
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.viewer.presenter.SearchAdapterPresenterDelegate
import app.atomofiron.searchboxapp.screens.viewer.presenter.TextViewerParams
import app.atomofiron.searchboxapp.screens.viewer.recycler.TextViewerAdapter
import kotlinx.coroutines.CoroutineScope

class TextViewerPresenter(
    params: TextViewerParams,
    scope: CoroutineScope,
    private val viewState: TextViewerViewState,
    router: TextViewerRouter,
    private val searchDelegate: SearchAdapterPresenterDelegate,
    private val interactor: TextViewerInteractor,
    preferenceStore: PreferenceStore,
    textViewerChannel: TextViewerChannel
) : BasePresenter<TextViewerViewModel, TextViewerRouter>(scope, router),
    TextViewerAdapter.TextViewerListener,
    FinderAdapterOutput by searchDelegate
{
    private var lineIndexMatchesMap: Map<Int, List<TextLineMatch>> = HashMap()
    private var matchesCount: Int? = null

    init {
        textViewerChannel.textFromFile.collect(scope) {
            scope.launch {
                viewState.textLines.value = it
            }
        }
        textViewerChannel.lineIndexMatches.collect(scope) {
            viewState.lineIndexMatches = it
        }
        textViewerChannel.lineIndexMatchesMap.collect(scope) {
            scope.launch {
                lineIndexMatchesMap = it
                viewState.matchesMap.value = it
            }
        }
        textViewerChannel.matchesCount.collect(scope) {
            scope.launch {
                matchesCount = it
                viewState.matchesCounter.value = matchesCount?.toLong()
                viewState.matchesCursor.value = null
            }
        }
        textViewerChannel.textFromFileLoading.collect(scope) {
            scope.launch {
                viewState.loading.value = it
            }
        }
        textViewerChannel.tasks.collect(scope) {
            scope.launch {
                viewState.setTasks(it)
            }
        }
        viewState.composition = preferenceStore.explorerItemComposition.value

        val queryParams = params.query?.let {
            FinderQueryParams(params.query, params.useRegex, params.ignoreCase)
        }
        val item = Node(params.path, content = NodeContent.File.Other)
        interactor.loadFile(item, queryParams) {
            viewState.item = item
        }
    }

    override fun onSubscribeData() = Unit

    override fun onLineVisible(index: Int) = interactor.onLineVisible(index)

    fun onSearchClick() = searchDelegate.show()

    fun onPreviousClick() = viewState.changeCursor(increment = false)

    fun onNextClick() {
        val success = viewState.changeCursor(increment = true)
        if (!success) {
            interactor.loadFileUpToLine(viewState.currentLineIndexCursor) {
                scope.launch {
                    viewState.changeCursor(increment = true)
                }
            }
            viewState.loading.value = true
        }
    }
}