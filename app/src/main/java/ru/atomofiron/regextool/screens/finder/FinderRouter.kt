package ru.atomofiron.regextool.screens.finder

import ru.atomofiron.regextool.common.base.BaseRouter
import ru.atomofiron.regextool.screens.explorer.ExplorerFragment

class FinderRouter : BaseRouter() {
    fun showExplorer() {
        switchScreen(addToBackStack = false) {
            it is ExplorerFragment
        }
    }
}