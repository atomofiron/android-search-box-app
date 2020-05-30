package ru.atomofiron.regextool.screens.viewer

import android.content.Context
import android.content.Intent
import app.atomofiron.common.arch.BasePresenter
import ru.atomofiron.regextool.injectable.interactor.TextViewerInteractor
import ru.atomofiron.regextool.injectable.store.PreferenceStore

class TextViewerPresenter(
        viewModel: TextViewerViewModel,
        router: TextViewerRouter,
        private val textViewerService: TextViewerInteractor,
        private val preferenceStore: PreferenceStore
) : BasePresenter<TextViewerViewModel, TextViewerRouter>(viewModel, router) {
    override fun onCreate(context: Context, intent: Intent) {

    }
}