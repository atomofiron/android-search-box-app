package app.atomofiron.searchboxapp.screens.root

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.insets.ViewGroupInsetsProxy
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.explorer.ExplorerFragment
import app.atomofiron.searchboxapp.screens.finder.FinderFragment

class RootFragment : Fragment(R.layout.fragment_root),
    BaseFragment<RootFragment, RootViewModel, RootPresenter> by BaseFragmentImpl()
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, RootViewModel::class, savedInstanceState)

        if (childFragmentManager.fragments.isEmpty()) {
            val explorer = ExplorerFragment()
            val finder = FinderFragment()
            childFragmentManager.beginTransaction()
                .add(R.id.main_fl_root, explorer)
                .hide(explorer)
                .add(R.id.main_fl_root, finder)
                .commit()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewGroupInsetsProxy.set(view)
    }
}