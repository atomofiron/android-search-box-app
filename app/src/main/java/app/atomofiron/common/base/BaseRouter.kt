package app.atomofiron.common.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import app.atomofiron.common.util.OneTimeBackStackListener
import ru.atomofiron.regextool.log2
import kotlin.reflect.KClass

abstract class BaseRouter {
    protected var fragment: Fragment? = null
    protected var activity: AppCompatActivity? = null
        get() = field ?: fragment?.activity as AppCompatActivity?

    protected val isDestroyed: Boolean get() =  fragment == null && activity == null
    protected var isBlocked = false

    protected val arguments: Bundle get() {
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
    protected fun <R> childManager(action: FragmentManager.() -> R): R {
        var manager = fragment?.childFragmentManager
        manager = manager ?: activity?.supportFragmentManager
        return manager!!.action()
    }
    protected fun <R> manager(action: FragmentManager.() -> R): R {
        //var manager = fragmentReference.get()?.parentFragmentManager
        val manager = activity?.supportFragmentManager
        return manager!!.action()
    }

    protected fun nextIntent(clazz: KClass<out Activity>): Intent {
        return context {
            Intent(this, clazz.java)
        }
    }

    fun onFragmentAttach(fragment: Fragment) {
        this.fragment = fragment
    }

    fun onActivityAttach(activity: AppCompatActivity) {
        this.activity = activity
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

    fun onBack(): Boolean {
        return manager {
            val lastVisible = fragments
                    .filter { it is Backable }
                    .findLast { !it.isHidden }
            when {
                (lastVisible as Backable?)?.onBack() == true -> true
                backStackEntryCount > 0 -> {
                    popBackStack()
                    true
                }
                fragments.size > 1 && fragments[0] != lastVisible -> {
                    beginTransaction().apply {
                        hide(lastVisible!!)
                        show(fragments[fragments.indexOf(lastVisible).dec()])
                        commit()
                    }
                    true
                }
                else -> false
            }
        }
    }
}