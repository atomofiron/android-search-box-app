package ru.atomofiron.regextool.screens.finder

import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.property.WeakProperty
import ru.atomofiron.regextool.screens.explorer.ExplorerFragment
import ru.atomofiron.regextool.screens.preferences.PreferenceFragment
import ru.atomofiron.regextool.screens.result.ResultFragment

class FinderRouter(fragment: WeakProperty<FinderFragment>) : BaseRouter(fragment) {
    fun showExplorer() {
        switchScreen(addToBackStack = false) {
            it is ExplorerFragment
        }
    }

    fun showSettings() = startScreen(PreferenceFragment())

    fun showResult(taskId: Long) = startScreen(ResultFragment.create(taskId))
}