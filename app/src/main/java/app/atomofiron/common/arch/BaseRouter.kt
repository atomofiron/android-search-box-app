package app.atomofiron.common.arch

import android.content.Context
import android.os.Bundle
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.*
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.R

abstract class BaseRouter(
    fragmentProperty: WeakProperty<Fragment>,
    private val activityProperty: WeakProperty<FragmentActivity> = WeakProperty(null),
) {
    companion object {
        const val RECIPIENT = "RECIPIENT"

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

    protected abstract val currentDestinationId: Int?

    constructor(activityProperty: WeakProperty<FragmentActivity>) : this(WeakProperty(), activityProperty)

    protected open val isCurrentDestination: Boolean
        get() = navigation {
            currentDestination?.id == currentDestinationId || currentDestination?.id == R.id.curtainFragment
        } == true

    val isWrongDestination: Boolean get() = !isCurrentDestination

    val fragment: Fragment? by fragmentProperty

    val activity: FragmentActivity? get() = fragment?.activity ?: activityProperty.value

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

    fun <I, O> register(
        contract: ActivityResultContract<I, O>,
        callback: ActivityResultCallback<O>,
    ): ActivityResultLauncher<I> = fragment {
        registerForActivityResult(contract, callback)
    }!!

    fun shouldShowRequestPermissionRationale(permission: String) = activity {
        ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
    }

    protected fun switchScreen(predicate: (Fragment) -> Boolean) {
        fragment {
            val fragments = parentFragmentManager.fragments
            parentFragmentManager.beginTransaction()
                .hide(this)
                .show(fragments.find(predicate)!!)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
        }
    }
}