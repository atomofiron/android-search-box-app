package ru.atomofiron.regextool.screens.root

import ru.atomofiron.regextool.R
import app.atomofiron.common.base.BaseFragment
import app.atomofiron.common.base.BaseRouter
import ru.atomofiron.regextool.screens.explorer.ExplorerFragment
import ru.atomofiron.regextool.screens.finder.FinderFragment
import ru.atomofiron.regextool.screens.preferences.PreferencesFragment

class RootRouter : BaseRouter() {
    override var fragmentContainerId: Int = R.id.root_fl

    fun showMain() {
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
            fragments
                    .filter { it is BaseFragment<*> || it is PreferencesFragment }
                    .forEach {
                        transaction.detach(it)
                        transaction.attach(it)
                    }
            transaction.commit()
        }
    }

    fun closeApp() {
        activity {
            finish()
        }
    }
}