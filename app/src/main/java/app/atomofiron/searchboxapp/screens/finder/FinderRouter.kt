package app.atomofiron.searchboxapp.screens.finder

import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.screens.explorer.ExplorerFragment
import app.atomofiron.searchboxapp.screens.preferences.PreferenceFragment
import app.atomofiron.searchboxapp.screens.result.ResultFragment

class FinderRouter(fragment: WeakProperty<FinderFragment>) : BaseRouter(fragment) {
    fun showExplorer() {
        switchScreen(addToBackStack = false) {
            it is ExplorerFragment
        }
    }

    fun showSettings() = startScreen(PreferenceFragment())

    fun showResult(taskId: Long) = startScreen(ResultFragment.create(taskId))
}