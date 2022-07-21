package app.atomofiron.searchboxapp.screens.root

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.explorer.ExplorerFragment
import app.atomofiron.searchboxapp.screens.finder.FinderFragment

class RootRouter(property: WeakProperty<Fragment>) : BaseRouter(property) {

    override val currentDestinationId = R.id.rootFragment

    init {
        fragment {
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
    }

    fun onBack(): Boolean {
        val result = fragment {
            val lastVisibleFragment = childFragmentManager.fragments.findLastVisibleFragment()
            var consumed = lastVisibleFragment?.onBack() ?: false
            if (!consumed && lastVisibleFragment?.let { it !is FinderFragment } == true) {
                childFragmentManager.switchScreen { it is FinderFragment }
                consumed = true
            }
            consumed
        }
        return result ?: false
    }
}