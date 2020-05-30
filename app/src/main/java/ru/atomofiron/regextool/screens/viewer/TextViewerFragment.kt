package ru.atomofiron.regextool.screens.viewer

import android.os.Bundle
import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.fragment.BaseFragment
import ru.atomofiron.regextool.R
import javax.inject.Inject
import kotlin.reflect.KClass

class TextViewerFragment : BaseFragment<TextViewerViewModel, TextViewerPresenter>() {
    companion object {
        private const val KEY_PATH = "KEY_PATH"

        fun openTextFile(path: String): Fragment {
            val bundle = Bundle()
            bundle.putString(KEY_PATH, path)
            val fragment = TextViewerFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
    override val viewModelClass: KClass<TextViewerViewModel> = TextViewerViewModel::class
    override val layoutId: Int = R.layout.fragment_text_viewer

    @Inject
    override lateinit var presenter: TextViewerPresenter

    override fun inject() = viewModel.inject(this)
}