package app.atomofiron.searchboxapp.screens.root

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.searchboxapp.R
import lib.atomofiron.android_window_insets_compat.insetsProxying

class RootFragment : Fragment(R.layout.fragment_root),
    BaseFragment<RootFragment, RootViewState, RootPresenter> by BaseFragmentImpl()
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, RootViewModel::class, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.insetsProxying()
        presenter.onChildrenCreated()
    }

    override fun onBack(): Boolean = presenter.onBack()
}