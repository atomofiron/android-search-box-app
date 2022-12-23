package app.atomofiron.searchboxapp.screens.template

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.searchboxapp.R
import lib.atomofiron.android_window_insets_compat.insetsProxying

class TemplateFragment : Fragment(R.layout.fragment_template),
    BaseFragment<TemplateFragment, TemplateViewState, TemplatePresenter> by BaseFragmentImpl()
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, TemplateViewModel::class, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.insetsProxying()
    }

    override fun onBack(): Boolean = false
}