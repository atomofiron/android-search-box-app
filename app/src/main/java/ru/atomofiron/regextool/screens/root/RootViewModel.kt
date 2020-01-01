package ru.atomofiron.regextool.screens.root

import android.app.Application
import android.content.Context
import android.content.Intent
import ru.atomofiron.regextool.common.base.BaseViewModel

class RootViewModel(app: Application) : BaseViewModel<RootRouter>(app) {
    override val router = RootRouter()

    override fun onCreate(context: Context, intent: Intent) {
        super.onCreate(context, intent)

        router.showMain()
    }
}