package app.atomofiron.searchboxapp.screens.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.util.navigation.CustomNavHostFragment
import app.atomofiron.common.util.property.WeakProperty

class MainRouter(activityProperty: WeakProperty<FragmentActivity>) : BaseRouter(activityProperty) {

    override val currentDestinationId = 0
    override val isCurrentDestination: Boolean = true

    private val fragments: List<Fragment>? get() = activity?.supportFragmentManager
        ?.fragments
        ?.first()
        ?.let {
            it as CustomNavHostFragment
        }?.childFragmentManager
        ?.fragments

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