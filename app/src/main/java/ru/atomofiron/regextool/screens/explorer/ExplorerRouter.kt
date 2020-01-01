package ru.atomofiron.regextool.screens.explorer

import ru.atomofiron.regextool.common.base.BaseRouter
import ru.atomofiron.regextool.screens.finder.FinderFragment

class ExplorerRouter : BaseRouter() {
    fun showFinder() {
        switchScreen(addToBackStack = false) {
            it is FinderFragment
        }
    }
}