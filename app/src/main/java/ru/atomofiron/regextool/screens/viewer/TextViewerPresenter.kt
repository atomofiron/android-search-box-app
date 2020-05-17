package ru.atomofiron.regextool.screens.viewer

import app.atomofiron.common.arch.BasePresenter
import ru.atomofiron.regextool.injectable.store.PreferenceStore

class TextViewerPresenter(
        viewModel: TextViewerViewModel,
        router: TextViewerRouter,
        private val preferenceStore: PreferenceStore
) : BasePresenter<TextViewerViewModel, TextViewerRouter>(viewModel, router)