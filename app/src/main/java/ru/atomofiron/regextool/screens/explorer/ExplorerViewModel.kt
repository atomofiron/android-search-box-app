package ru.atomofiron.regextool.screens.explorer

import android.app.Application
import ru.atomofiron.regextool.common.base.BaseViewModel

class ExplorerViewModel(app: Application) : BaseViewModel<ExplorerRouter>(app) {
    override val router = ExplorerRouter()

    fun onBookmarksOptionSelected() {

    }

    fun onSearchOptionSelected() {
        router.showFinder()
    }

    fun onConfigOptionSelected() {

    }

    fun onSettingsOptionSelected() {

    }
}