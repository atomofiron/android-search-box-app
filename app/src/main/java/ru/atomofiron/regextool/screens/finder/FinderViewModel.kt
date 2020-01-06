package ru.atomofiron.regextool.screens.finder

import android.app.Application
import ru.atomofiron.regextool.common.base.BaseViewModel

class FinderViewModel(app: Application) : BaseViewModel<FinderRouter>(app) {
    override val router = FinderRouter()

    fun onExplorerOptionSelected() {
        router.showExplorer()
    }

    fun onConfigOptionSelected() {

    }

    fun onSettingsOptionSelected() {

    }
}