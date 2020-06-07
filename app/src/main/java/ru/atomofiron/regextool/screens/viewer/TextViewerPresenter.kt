package ru.atomofiron.regextool.screens.viewer

import android.content.Context
import android.content.Intent
import app.atomofiron.common.arch.BasePresenter
import ru.atomofiron.regextool.injectable.channel.TextViewerChannel
import ru.atomofiron.regextool.injectable.interactor.TextViewerInteractor
import ru.atomofiron.regextool.model.finder.FinderQueryParams
import ru.atomofiron.regextool.model.textviewer.TextLine
import ru.atomofiron.regextool.screens.viewer.recycler.TextViewerAdapter

class TextViewerPresenter(
        viewModel: TextViewerViewModel,
        router: TextViewerRouter,
        private val interactor: TextViewerInteractor,
        textViewerChannel: TextViewerChannel
) : BasePresenter<TextViewerViewModel, TextViewerRouter>(viewModel, router), TextViewerAdapter.TextViewerListener {
    private lateinit var globalMatches: List<List<TextLine.Match>?>
    private var localMatches: List<List<TextLine.Match>?>? = null

    init {
        textViewerChannel.textFromFile.addObserver(onClearedCallback) {
            viewModel.textLines.postValue(it)
            globalMatches = it.map { match -> match.matches }
            viewModel.matches.postValue(globalMatches)
        }
        textViewerChannel.localMatches.addObserver(onClearedCallback) {
            localMatches = it
            updateMatches()
        }
        textViewerChannel.textFromFileLoading.addObserver(onClearedCallback) {
            viewModel.loading.postValue(it)
        }
    }

    private fun updateMatches() = when (localMatches) {
        null -> viewModel.matches.postValue(globalMatches)
        else -> viewModel.matches.postValue(localMatches)
    }

    override fun onCreate(context: Context, intent: Intent) {
        val path = intent.getStringExtra(TextViewerFragment.KEY_PATH)!!
        val query = intent.getStringExtra(TextViewerFragment.KEY_QUERY)
        val useRegex = intent.getBooleanExtra(TextViewerFragment.KEY_USE_SU, false)
        val ignoreCase = intent.getBooleanExtra(TextViewerFragment.KEY_IGNORE_CASE, false)
        val params = query?.let { FinderQueryParams(it, useRegex, ignoreCase) }
        // todo next
        interactor.loadFile(path, params)
    }

    override fun onLineVisible(index: Int) = interactor.onLineVisible(index)
}