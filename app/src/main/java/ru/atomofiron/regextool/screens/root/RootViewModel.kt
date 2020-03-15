package ru.atomofiron.regextool.screens.root

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import app.atomofiron.common.base.BaseViewModel
import app.atomofiron.common.util.SingleLiveEvent
import ru.atomofiron.regextool.iss.store.SettingsStore
import ru.atomofiron.regextool.model.AppOrientation
import ru.atomofiron.regextool.model.AppTheme
import ru.atomofiron.regextool.screens.root.util.tasks.XTask

class RootViewModel(app: Application) : BaseViewModel<RootRouter>(app) {
    override val router = RootRouter()

    val showExitSnackbar = SingleLiveEvent<Unit>()
    val setTheme = SingleLiveEvent<AppTheme>()
    val setOrientation = SingleLiveEvent<AppOrientation>()
    val tasks = MutableLiveData<List<XTask>>()
    var sbExitIsShown: Boolean = false

    override fun onCreate(context: Context, intent: Intent) {
        super.onCreate(context, intent)

        router.showMain()

        SettingsStore.appTheme.addObserver(onClearedCallback) {
            setTheme.invoke(it)
            router.reattachFragments()
        }
        SettingsStore.appOrientation.addObserver(onClearedCallback) {
            setOrientation.invoke(it)
        }
        tasks.value = Array(16) { XTask() }.toList()
    }

    fun onJoystickClick() {
        when {
            router.onBack() -> Unit
            else -> showExitSnackbar()
        }
    }

    fun onExitClick() = router.closeApp()

    override fun onBackButtonClick(): Boolean {
        when {
            super.onBackButtonClick() -> Unit
            sbExitIsShown -> router.closeApp()
            else -> showExitSnackbar()
        }
        return true
    }
}