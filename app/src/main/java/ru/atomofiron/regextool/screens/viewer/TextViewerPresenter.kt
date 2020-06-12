package ru.atomofiron.regextool.screens.viewer

import android.content.Context
import android.content.Intent
import app.atomofiron.common.arch.BasePresenter
import ru.atomofiron.regextool.injectable.channel.TextViewerChannel
import ru.atomofiron.regextool.injectable.interactor.TextViewerInteractor
import ru.atomofiron.regextool.model.finder.FinderQueryParams
import ru.atomofiron.regextool.model.textviewer.TextLineMatch
import ru.atomofiron.regextool.screens.viewer.recycler.TextViewerAdapter

class TextViewerPresenter(
        viewModel: TextViewerViewModel,
        router: TextViewerRouter,
        private val interactor: TextViewerInteractor,
        textViewerChannel: TextViewerChannel
) : BasePresenter<TextViewerViewModel, TextViewerRouter>(viewModel, router), TextViewerAdapter.TextViewerListener {
    private var globalMatchesMap: Map<Int, List<TextLineMatch>> = HashMap()
    private var localMatchesMap: Map<Int, List<TextLineMatch>>? = null

    private var globalMatchesCount: Int? = null
    private var localMatchesCount: Int? = null

    init {
        textViewerChannel.textFromFile.addObserver(onClearedCallback) {
            viewModel.textLines.postValue(it)
        }
        textViewerChannel.globalMatches.addObserver(onClearedCallback) {
            viewModel.globalMatches = it
        }
        textViewerChannel.localMatches.addObserver(onClearedCallback) {
            viewModel.localMatches = it
        }
        textViewerChannel.globalMatchesMap.addObserver(onClearedCallback) {
            globalMatchesMap = it
            updateMatchesMap()
        }
        textViewerChannel.localMatchesMap.addObserver(onClearedCallback) {
            localMatchesMap = it
            updateMatchesMap()
        }
        textViewerChannel.globalMatchesCount.addObserver(onClearedCallback) {
            globalMatchesCount = it
            updateMatchesCounter()
        }
        textViewerChannel.localMatchesCount.addObserver(onClearedCallback) {
            localMatchesCount = it
            updateMatchesCounter()
        }
        textViewerChannel.textFromFileLoading.addObserver(onClearedCallback) {
            viewModel.loading.postValue(it)
        }
    }

    private fun updateMatchesMap() = when (localMatchesMap) {
        null -> viewModel.matchesMap.postValue(globalMatchesMap)
        else -> viewModel.matchesMap.postValue(localMatchesMap)
    }

    private fun updateMatchesCounter() = when (localMatchesCount) {
        null -> viewModel.matchesCounter.postValue(globalMatchesCount?.toLong())
        else -> viewModel.matchesCounter.postValue(localMatchesCount?.toLong())
    }

    override fun onCreate(context: Context, intent: Intent) {
        val path = intent.getStringExtra(TextViewerFragment.KEY_PATH)!!
        val query = intent.getStringExtra(TextViewerFragment.KEY_QUERY)
        val useRegex = intent.getBooleanExtra(TextViewerFragment.KEY_USE_SU, false)
        val ignoreCase = intent.getBooleanExtra(TextViewerFragment.KEY_IGNORE_CASE, false)
        val params = query?.let { FinderQueryParams(it, useRegex, ignoreCase) }
        interactor.loadFile(path, params)
    }

    override fun onLineVisible(index: Int) = interactor.onLineVisible(index)

    fun onSearchClick() {
    }

    fun onPreviousClick() = viewModel.changeCursor(increment = false)

    fun onNextClick() {
        val success = viewModel.changeCursor(increment = true)
        if (!success) {
            interactor.loadFileUpToLine(viewModel.currentLineIndexCursor) {
                viewModel.changeCursor(increment = true)
            }
            viewModel.loading.value = true
        }
    }
}