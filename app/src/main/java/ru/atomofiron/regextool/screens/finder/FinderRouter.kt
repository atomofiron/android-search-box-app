package ru.atomofiron.regextool.screens.finder

import app.atomofiron.common.base.BaseRouter
import ru.atomofiron.regextool.screens.explorer.ExplorerFragment
import ru.atomofiron.regextool.screens.preferences.PreferencesFragment

class FinderRouter : BaseRouter() {
    override val indexFromEnd: Int = 1

    fun showExplorer() {
        switchScreen(addToBackStack = false) {
            it is ExplorerFragment
        }
    }

    fun showSettings() = startScreen(PreferencesFragment())
}