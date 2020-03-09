package ru.atomofiron.regextool.screens.finder

import app.atomofiron.common.base.BaseRouter
import ru.atomofiron.regextool.screens.explorer.ExplorerFragment
import ru.atomofiron.regextool.screens.preferences.PreferenceFragment

class FinderRouter : BaseRouter() {
    fun showExplorer() {
        switchScreen(addToBackStack = false) {
            it is ExplorerFragment
        }
    }

    fun showSettings() = startScreen(PreferenceFragment())
}