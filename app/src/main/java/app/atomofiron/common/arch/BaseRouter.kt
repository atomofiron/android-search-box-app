package app.atomofiron.common.arch

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import app.atomofiron.common.util.OneTimeBackStackListener
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.common.util.setOneTimeBackStackListener
import ru.atomofiron.regextool.log2

abstract class BaseRouter(viewProperty: WeakProperty<out Any>) {
    private val fragmentProperty: WeakProperty<Fragment> = when (viewProperty.value) {
        is Fragment ->
            @Suppress("UNCHECKED_CAST")
            viewProperty as WeakProperty<Fragment>
        else -> WeakProperty()
    }
    private val activityProperty: WeakProperty<AppCompatActivity> = when (viewProperty.value) {
        is AppCompatActivity ->
            @Suppress("UNCHECKED_CAST")
            viewProperty as WeakProperty<AppCompatActivity>
        else -> object : WeakProperty<AppCompatActivity>() {
            override val value: AppCompatActivity?
                get() = fragmentProperty.value?.requireActivity() as AppCompatActivity?
        }
    }

    protected val fragment: Fragment? by fragmentProperty
    protected val activity: AppCompatActivity? by activityProperty

    protected val isDestroyed: Boolean get() = fragment == null && activity == null
    protected var isBlocked = false

    protected val arguments: Bundle
        get() {
            var arguments = fragment?.arguments
            arguments = arguments ?: activity?.intent?.extras
            return arguments!!
        }

    protected open var fragmentContainerId: Int = 0
        get() {
            if (field == 0) {
                field = (fragment!!.requireView().parent as View).id
            }
            return field
        }

    protected fun <R> context(action: Context.() -> R): R = activity!!.action()
    protected fun <R> fragment(action: Fragment.() -> R): R = fragment!!.action()
    protected fun <R> activity(action: AppCompatActivity.() -> R): R = activity!!.action()
    protected fun <R> manager(action: FragmentManager.() -> R): R {
        return activity!!.supportFragmentManager.action()
    }

    open fun onAttachChildFragment(childFragment: Fragment) = Unit

    protected fun startScreen(vararg fragmentsArg: Fragment,
                              addToBackStack: Boolean = true,
                              runOnCommit: (() -> Unit)? = null) {
        if (isDestroyed || isBlocked) {
            return
        }
        manager {
            val filteredFragments = filterAddedFragments(this, fragmentsArg)
            if (filteredFragments.isEmpty()) {
                return@manager
            }
            isBlocked = true
            val current = fragments.find { !it.isHidden }
            beginTransaction().apply {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                if (current != null) {
                    hide(current)
                }
                filteredFragments.forEach {
                    val tag = it.javaClass.simpleName
                    add(fragmentContainerId, it, tag)
                }
                if (addToBackStack) {
                    addToBackStack(null)
                    OneTimeBackStackListener(this@manager) {
                        isBlocked = false
                        runOnCommit?.invoke()
                    }
                } else {
                    runOnCommit {
                        isBlocked = false
                        runOnCommit?.invoke()
                    }
                }
                commit()
            }
        }
    }

    private fun filterAddedFragments(manager: FragmentManager, fragments: Array<out Fragment>): List<Fragment> {
        return fragments.filter {
            val tag = it.javaClass.simpleName
            when (manager.fragments.find { added -> added.tag == tag } == null) {
                true -> true
                else -> {
                    log2("Fragment with tag = $tag is already added!")
                    false
                }
            }
        }
    }

    protected fun switchScreen(addToBackStack: Boolean = true, predicate: (Fragment) -> Boolean) {
        if (isDestroyed || isBlocked) {
            return
        }
        manager {
            val current = fragments.find { !it.isHidden }!!
            if (predicate(current)) {
                return@manager
            }
            isBlocked = true
            beginTransaction()
                    .hide(current)
                    .show(fragments.find(predicate)!!)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .apply {
                        if (addToBackStack) {
                            addToBackStack(null)
                            OneTimeBackStackListener(this@manager) {
                                isBlocked = false
                            }
                        } else {
                            runOnCommit {
                                isBlocked = false
                            }
                        }
                    }
                    .commit()
        }
    }

    fun popScreen() {
        if (isDestroyed || isBlocked) {
            return
        }
        manager {
            if (backStackEntryCount == 0) {
                return@manager
            }
            isBlocked = true
            setOneTimeBackStackListener {
                isBlocked = false
            }
            popBackStack()
        }
    }
}