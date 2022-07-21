package app.atomofiron.common.arch

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.*
import app.atomofiron.common.util.property.MutableWeakProperty
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.R

abstract class BaseRouter(
    fragmentProperty: WeakProperty<Fragment>,
    protected val activityProperty: WeakProperty<FragmentActivity> = activityProperty(fragmentProperty),
) {
    companion object {
        const val RECIPIENT = "RECIPIENT"

        private fun activityProperty(fragmentProperty: WeakProperty<Fragment>): WeakProperty<FragmentActivity> {
            val activityProperty = MutableWeakProperty<FragmentActivity>()
            fragmentProperty.observe { fragment ->
                activityProperty.value = fragment?.activity
            }
            return activityProperty
        }

        val navOptions: NavOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.fragment_enter_scale)
            .setExitAnim(R.anim.fragment_nothing)
            .setPopEnterAnim(R.anim.fragment_nothing)
            .setPopExitAnim(R.anim.fragment_exit_scale)
            .setLaunchSingleTop(true)
            .build()

        val navExitOptions: NavOptions
            get() = NavOptions.Builder()
                .setPopEnterAnim(R.anim.fragment_nothing)
                .setPopExitAnim(R.anim.fragment_exit_scale)
                .setLaunchSingleTop(true)
                .build()

        val curtainOptions: NavOptions get() = NavOptions.Builder().setLaunchSingleTop(true).build()
    }

    protected abstract val currentDestinationId: Int

    constructor(activityProperty: WeakProperty<FragmentActivity>) : this(WeakProperty(), activityProperty)

    protected open val isCurrentDestination: Boolean
        get() = navigation {
            currentDestination?.id == currentDestinationId || currentDestination?.id == R.id.curtainFragment
        } == true

    val isWrongDestination: Boolean get() = !isCurrentDestination

    val fragment: Fragment? by fragmentProperty

    val activity: FragmentActivity? by activityProperty

    fun <R> fragment(action: Fragment.() -> R): R? = fragment?.run(action)

    fun <R> activity(action: FragmentActivity.() -> R): R? = activity?.run(action)

    fun <R> context(action: Context.() -> R): R? = (fragment?.context ?: activity)?.run(action)

    fun <T> navigation(action: NavController.() -> T): T? {
        return activity?.run {
            Navigation.findNavController(this, R.id.nav_host_fragment).run(action)
        }
    }

    fun navigateBack() {
        navigation {
            navigateUp()
        }
    }

    fun navigate(actionId: Int, args: Bundle? = null, navOptions: NavOptions = Companion.navOptions) {
        navigation {
            if (isCurrentDestination) {
                navigate(actionId, args, navOptions)
            }
        }
    }

    protected fun FragmentManager.switchScreen(predicate: (Fragment) -> Boolean) {
        val lastVisible = fragments.findLastVisibleFragment()
        val target = fragments.find(predicate)
        if (lastVisible === target) return
        beginTransaction()
            .apply {
                lastVisible?.let { hide(it as Fragment) }
            }
            .show(target!!)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }

    protected fun List<Fragment>?.findLastVisibleFragment() = this?.filter {
        it is BaseFragment<*,*,*>
    }?.run {
        lastOrNull { it.isVisible } ?: lastOrNull { !it.isHidden }
    } as? BaseFragment<*,*,*>
}