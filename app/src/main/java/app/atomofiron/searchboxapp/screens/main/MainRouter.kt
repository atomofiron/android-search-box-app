package app.atomofiron.searchboxapp.screens.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.util.navigation.CustomNavHostFragment
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.poop
import app.atomofiron.searchboxapp.screens.explorer.ExplorerFragment
import app.atomofiron.searchboxapp.screens.finder.FinderFragment

class MainRouter(activityProperty: WeakProperty<FragmentActivity>) : BaseRouter(activityProperty) {

    override val currentDestinationId: Int? = null
    override val isCurrentDestination: Boolean = true

    private val fragments: List<Fragment>? get() = activity?.supportFragmentManager
        ?.fragments
        ?.first()
        ?.let {
            it as CustomNavHostFragment
        }?.childFragmentManager
        ?.fragments

    fun showMainIfNeeded() {
        poop("")
        activity {
            val isEmpty = supportFragmentManager.fragments.size == 1 // CustomNavHostFragment
            if (!isEmpty) return@activity
            val explorerFragment = ExplorerFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.main_fl_main, explorerFragment)
                .hide(explorerFragment)
                .add(R.id.main_fl_main, FinderFragment())
                .commit()
        }
    }

    fun reattachFragments() {
        /* todo manager {
            val transaction = beginTransaction()
            fragments.filter{ it is BaseFragment<*, *> || it is BasePreferenceFragment<*, *> }
                    .forEach {
                        transaction.detach(it)
                        transaction.attach(it)
                    }
            transaction.commit()
        }*/
    }

    fun onBack(): Boolean {
        val lastVisibleFragment = fragments?.lastOrNull { it.isVisible } as? BaseFragment<*, *, *>
        val consumed = lastVisibleFragment?.onBack() == true
        return consumed || navigation {
            navigateUp()
        } ?: false
    }

    fun closeApp() {
        activity {
            finish()
        }
    }
}