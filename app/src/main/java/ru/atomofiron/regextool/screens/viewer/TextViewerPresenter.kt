package ru.atomofiron.regextool.screens.viewer

import android.content.Context
import android.content.Intent
import app.atomofiron.common.arch.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.injectable.channel.TextViewerChannel
import ru.atomofiron.regextool.injectable.interactor.TextViewerInteractor
import ru.atomofiron.regextool.injectable.store.PreferenceStore
import ru.atomofiron.regextool.model.explorer.MutableXFile
import ru.atomofiron.regextool.model.finder.FinderQueryParams
import ru.atomofiron.regextool.model.textviewer.TextLineMatch
import ru.atomofiron.regextool.screens.finder.adapter.FinderAdapterOutput
import ru.atomofiron.regextool.screens.viewer.presenter.SearchAdapterPresenterDelegate
import ru.atomofiron.regextool.screens.viewer.recycler.TextViewerAdapter

class TextViewerPresenter(
        private val scope: CoroutineScope,
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
        textViewerChannel.textFromFile.addObserver(onClearedCallback) {
            scope.launch {
                viewModel.textLines.value = it
            }
        }
        textViewerChannel.lineIndexMatches.addObserver(onClearedCallback) {
            viewModel.lineIndexMatches = it
        }
        textViewerChannel.lineIndexMatchesMap.addObserver(onClearedCallback) {
            scope.launch {
                lineIndexMatchesMap = it
                viewModel.matchesMap.value = it
            }
        }
        textViewerChannel.matchesCount.addObserver(onClearedCallback) {
            scope.launch {
                matchesCount = it
                viewModel.matchesCounter.value = matchesCount!!.toLong()
            }
        }
        textViewerChannel.textFromFileLoading.addObserver(onClearedCallback) {
            scope.launch {
                viewModel.loading.value = it
            }
        }
        textViewerChannel.localTasks.addObserver(onClearedCallback) {
            scope.launch {
                viewModel.setTasks(it)
            }
        }
        viewModel.composition = preferenceStore.explorerItemComposition.entity
    }

    override fun onCreate(context: Context, intent: Intent) {
        val path = intent.getStringExtra(TextViewerFragment.KEY_PATH)!!
        val query = intent.getStringExtra(TextViewerFragment.KEY_QUERY)
        val useRegex = intent.getBooleanExtra(TextViewerFragment.KEY_USE_SU, false)
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