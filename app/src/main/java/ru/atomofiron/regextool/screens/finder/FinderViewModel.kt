package ru.atomofiron.regextool.screens.finder

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import ru.atomofiron.regextool.common.base.BaseViewModel
import ru.atomofiron.regextool.iss.store.SettingsStore

class FinderViewModel(app: Application) : BaseViewModel<FinderRouter>(app) {
    override val router = FinderRouter()
    val historyDrawerGravity = MutableLiveData<Int>()

    private val dockGravityChanged: (Int) -> Unit = { gravity ->
        historyDrawerGravity.value = gravity
    }

    override fun onCreate(context: Context, intent: Intent) {
        super.onCreate(context, intent)

        SettingsStore
                .dockGravity
                .addObserver(onClearedCallback, dockGravityChanged)
    }

    fun onDockGravityChange(gravity: Int) = SettingsStore.dockGravity.push(gravity)

    fun onExplorerOptionSelected() {
        router.showExplorer()
    }

    fun onConfigOptionSelected() {

    }

    fun onSettingsOptionSelected() = router.showSettings()
}