package ru.atomofiron.regextool.screens.root

import app.atomofiron.common.base.BaseRouter
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.screens.explorer.ExplorerFragment
import ru.atomofiron.regextool.screens.finder.FinderFragment

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
            fragments.forEach {
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