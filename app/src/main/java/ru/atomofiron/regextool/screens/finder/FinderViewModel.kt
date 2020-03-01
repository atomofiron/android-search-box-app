package ru.atomofiron.regextool.screens.finder

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import ru.atomofiron.regextool.channel.PreferencesChannel
import ru.atomofiron.regextool.common.base.BaseViewModel
import ru.atomofiron.regextool.common.util.ReadyLiveData
import ru.atomofiron.regextool.common.util.SingleLiveEvent
import ru.atomofiron.regextool.iss.service.model.MutableXFile
import ru.atomofiron.regextool.iss.store.SettingsStore
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem.*

class FinderViewModel(app: Application) : BaseViewModel<FinderRouter>(app) {
    override val router = FinderRouter()
    val historyDrawerGravity = MutableLiveData<Int>()
    val state = ReadyLiveData<List<FinderStateItem>>()
    val reloadHistory = SingleLiveEvent<Unit>()

    init {
        val items = ArrayList<FinderStateItem>()
        items.add(SearchAndReplace())
        val characters = SettingsStore.specialCharacters.entity
        items.add(SpecialCharacters(characters))
        items.add(Config())
        items.add(ProgressItem(777, "9/36"))
        items.add(ResultItem(777, MutableXFile("-rwxrwxrwx", "atomofiron", "atomofiron", "7B", "DATE", "TIME", "some_file", "", false, "/sdcard/search/path/")))
        state.value = items
    }

    override fun onCreate(context: Context, intent: Intent) {
        super.onCreate(context, intent)

        SettingsStore
                .dockGravity
                .addObserver(onClearedCallback) { gravity ->
                    historyDrawerGravity.value = gravity
                }
        SettingsStore.specialCharacters.addObserver(onClearedCallback) {
            updateItem(FinderStateItem.CHARACTERS_POSITION, SpecialCharacters(it))
        }
        PreferencesChannel.historyImportedEvent.addObserver(onClearedCallback) {
            reloadHistory.invoke()
        }
    }

    fun onConfigChange(item: Config) {
        updateItem(FinderStateItem.SEARCH_POSITION, SearchAndReplace(item.replaceEnabled))
    }

    fun onDockGravityChange(gravity: Int) = SettingsStore.dockGravity.push(gravity)

    fun onExplorerOptionSelected() {
        router.showExplorer()
    }

    fun onConfigOptionSelected() {
        var item = state.value[FinderStateItem.CONFIG_POSITION] as Config
        item = item.copy(configVisible = item.configVisible)
        updateItem(FinderStateItem.CONFIG_POSITION, item)
    }

    fun onSettingsOptionSelected() = router.showSettings()

    private fun updateItem(index: Int, item: FinderStateItem) {
        val items = ArrayList<FinderStateItem>(state.value)
        items.removeAt(index)
        items.add(index, item)
        state.value = items
    }
}