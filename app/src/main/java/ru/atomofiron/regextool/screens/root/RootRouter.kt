package ru.atomofiron.regextool.screens.root

import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.arch.fragment.BaseFragment
import app.atomofiron.common.arch.fragment.BasePreferenceFragment
import app.atomofiron.common.arch.view.Backable
import app.atomofiron.common.util.property.WeakProperty
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.screens.explorer.ExplorerFragment
import ru.atomofiron.regextool.screens.finder.FinderFragment

class RootRouter(activity: WeakProperty<RootActivity>) : BaseRouter(activity) {
    override var fragmentContainerId: Int = R.id.root_fl

    fun showMainIfEmpty() {
        val fragmentsDoNotAdded = manager {
            fragments.isEmpty()
        }
        if (fragmentsDoNotAdded) {
            startScreen(FinderFragment(), addToBackStack = false) {
                startScreen(ExplorerFragment(), addToBackStack = false) {
                    switchScreen(addToBackStack = false) { it is FinderFragment }
                }
            }
        }
    }

    fun reattachFragments() {
        manager {
            val transaction = beginTransaction()
            fragments.filter{ it is BaseFragment<*,*> || it is BasePreferenceFragment<*, *> }
                    .forEach {
                        transaction.detach(it)
                        transaction.attach(it)
                    }
            transaction.commit()
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

    fun closeApp() {
        activity {
            finish()
        }
    }
}