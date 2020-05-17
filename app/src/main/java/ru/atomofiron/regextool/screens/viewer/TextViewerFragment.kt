package ru.atomofiron.regextool.screens.viewer

import app.atomofiron.common.arch.fragment.BaseFragment
import ru.atomofiron.regextool.R
import javax.inject.Inject
import kotlin.reflect.KClass

class TextViewerFragment : BaseFragment<TextViewerViewModel, TextViewerPresenter>() {
    override val viewModelClass: KClass<TextViewerViewModel> = TextViewerViewModel::class
    override val layoutId: Int = R.layout.fragment_text_viewer

    @Inject
    override lateinit var presenter: TextViewerPresenter

    override fun inject() = viewModel.inject(this)
}