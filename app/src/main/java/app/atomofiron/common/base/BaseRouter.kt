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
import java.lang.ref.WeakReference
import kotlin.reflect.KClass

abstract class BaseRouter {
    private lateinit var fragmentReference: WeakReference<Fragment?>
    private lateinit var activityReference: WeakReference<AppCompatActivity>

    protected val fragment: Fragment? get() = fragmentReference.get()
    protected val activity: AppCompatActivity? get() = activityReference.get()
    protected val isDestroyed: Boolean get() =  fragment == null && activity == null
    protected var isBlocked = false

    protected val arguments: Bundle get() {
        var arguments = fragmentReference.get()?.arguments
        arguments = arguments ?: activityReference.get()?.intent?.extras
        return arguments!!
    }

    protected open var fragmentContainerId: Int = 0
        get() {
            if (field == 0) {
                field = (fragment!!.view!!.parent as View).id
            }
            return field
        }

    protected fun context(action: Context.() -> Unit) = activityReference.get()!!.action()
    protected fun fragment(action: Fragment.() -> Unit) = fragmentReference.get()!!.action()
    protected fun activity(action: AppCompatActivity.() -> Unit) = activityReference.get()!!.action()
    protected fun childManager(action: FragmentManager.() -> Unit) {
        var manager = fragmentReference.get()?.childFragmentManager
        manager = manager ?: activityReference.get()?.supportFragmentManager
        manager!!.action()
    }
    protected fun <T> manager(action: FragmentManager.() -> T): T {
        //var manager = fragmentReference.get()?.parentFragmentManager
        val manager = activityReference.get()?.supportFragmentManager
        return manager!!.action()
    }

    protected fun nextIntent(clazz: KClass<out Activity>): Intent {
        var intent: Intent? = null
        context {
            intent = Intent(this, clazz.java)
        }
        return intent!!
    }

    fun onFragmentAttach(fragment: Fragment) {
        fragmentReference = WeakReference(fragment)
        activityReference = WeakReference(fragment.activity as AppCompatActivity)
    }

    fun onActivityAttach(activity: AppCompatActivity) {
        fragmentReference = WeakReference(null)
        activityReference = WeakReference(activity)
    }

    fun onViewDestroy() {
        fragmentReference.clear()
        activityReference.clear()
    }

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
            val lastVisible = fragments.findLast { !it.isHidden }
            when {
                (lastVisible as? BaseFragment<*>)?.onBack() == true -> true
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