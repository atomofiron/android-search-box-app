package app.atomofiron.searchboxapp.screens.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.util.navigation.CustomNavHostFragment
import app.atomofiron.common.util.property.WeakProperty

class MainRouter(activityProperty: WeakProperty<FragmentActivity>) : BaseRouter(activityProperty) {

    override val currentDestinationId = 0
    override val isCurrentDestination: Boolean = true

    private val fragmentManager: FragmentManager? get() = activity?.supportFragmentManager
        ?.fragments
        ?.first()
        ?.let {
            it as CustomNavHostFragment
        }?.childFragmentManager

    private val fragments: List<Fragment>? get() = fragmentManager?.fragments

    val lastVisibleFragment get() = fragments?.filter { it is BaseFragment<*,*,*> }?.run {
        lastOrNull { it.isVisible } ?: lastOrNull { !it.isHidden }
    } as? BaseFragment<*,*,*>

    fun reattachFragments() {
        fragmentManager?.run {
            beginTransaction().run {
                fragments.forEach { detach(it) }
                commit()
            }
            beginTransaction().run {
                fragments.forEach { attach(it) }
                commit()
            }
        }
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