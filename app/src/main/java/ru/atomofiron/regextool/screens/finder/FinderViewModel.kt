package ru.atomofiron.regextool.screens.finder

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import ru.atomofiron.regextool.channel.PreferencesChannel
import ru.atomofiron.regextool.common.base.BaseViewModel
import ru.atomofiron.regextool.common.util.ReadyLiveData
import ru.atomofiron.regextool.common.util.SingleLiveEvent
import ru.atomofiron.regextool.iss.store.SettingsStore
import ru.atomofiron.regextool.screens.finder.model.FinderState

class FinderViewModel(app: Application) : BaseViewModel<FinderRouter>(app) {
    override val router = FinderRouter()
    val historyDrawerGravity = MutableLiveData<Int>()
    val state = ReadyLiveData<FinderState>()
    val reloadHistory = SingleLiveEvent<Unit>()

    init {
        state.value = FinderState(false, arrayOf(), false, ArrayList(), ArrayList())
    }

    override fun onCreate(context: Context, intent: Intent) {
        super.onCreate(context, intent)

        SettingsStore
                .dockGravity
                .addObserver(onClearedCallback) { gravity ->
                    historyDrawerGravity.value = gravity
                }
        SettingsStore.specialCharacters.addObserver(onClearedCallback) {
            state.value = state.value.copy(characters = it.split(" ").toTypedArray())
        }
        PreferencesChannel.historyImportedEvent.addObserver(onClearedCallback) {
            reloadHistory.invoke()
        }
    }

    fun onRepleceEnabledClick() {
        state.value = state.value.copy(replaceEnabled = !state.value.replaceEnabled)
    }

    fun onDockGravityChange(gravity: Int) = SettingsStore.dockGravity.push(gravity)

    fun onExplorerOptionSelected() {
        router.showExplorer()
    }

    fun onConfigOptionSelected() {
        state.value = state.value.copy(configVisible = !state.value.configVisible)
    }

    fun onSettingsOptionSelected() = router.showSettings()
}