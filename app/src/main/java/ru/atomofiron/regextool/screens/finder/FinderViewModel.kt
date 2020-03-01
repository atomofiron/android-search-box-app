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
        items.add(SearchAndReplaceItem())
        val characters = SettingsStore.specialCharacters.entity
        items.add(SpecialCharactersItem(characters))
        items.add(ConfigItem())
        items.add(TestItem())
        items.add(ProgressItem(777, "9/36"))
        for (i in 1L..30L)
        items.add(ResultItem(i, MutableXFile("-rwxrwxrwx", "atomofiron", "atomofiron", "7B", "DATE", "TIME", "some_file", "", false, "/sdcard/search/path/some_file")))
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
            updateItem(FinderStateItem.CHARACTERS_POSITION, SpecialCharactersItem(it))
        }
        PreferencesChannel.historyImportedEvent.addObserver(onClearedCallback) {
            reloadHistory.invoke()
        }
    }

    fun onConfigChange(newItem: ConfigItem) {
        val item = state.value[FinderStateItem.CONFIG_POSITION] as ConfigItem

        val ignoreCaseChanged = item.ignoreCase xor newItem.ignoreCase
        val replaceEnabledChanged = item.replaceEnabled xor newItem.replaceEnabled
        val useRegexpChanged = item.useRegexp xor newItem.useRegexp

        if (replaceEnabledChanged || useRegexpChanged) {
            val it = SearchAndReplaceItem(newItem.replaceEnabled, newItem.useRegexp)
            updateItem(FinderStateItem.SEARCH_POSITION, it)
        }

        updateItem(FinderStateItem.CONFIG_POSITION) { newItem }

        if (ignoreCaseChanged || replaceEnabledChanged || useRegexpChanged) {
            updateItem(FinderStateItem.TEST_POSITION) {
                it as TestItem
                it.copy(useRegexp = newItem.useRegexp,
                        ignoreCase = newItem.ignoreCase,
                        multilineSearch = newItem.multilineSearch)
            }
        }
    }

    fun onSearchChange(value: String) {
        updateItem(FinderStateItem.TEST_POSITION) {
            it as TestItem
            it.copy(searchQuery = value)
        }
    }

    fun onDockGravityChange(gravity: Int) = SettingsStore.dockGravity.push(gravity)

    fun onExplorerOptionSelected() {
        router.showExplorer()
    }

    fun onConfigOptionSelected() {
        updateItem(FinderStateItem.CONFIG_POSITION) {
            it as ConfigItem
            it.copy(configVisible = !it.configVisible)
        }
    }

    fun onSettingsOptionSelected() = router.showSettings()

    private fun updateItem(index: Int, item: FinderStateItem) = updateItem(index) { item }

    private fun updateItem(index: Int, action: (FinderStateItem) -> FinderStateItem) {
        val items = ArrayList<FinderStateItem>(state.value)
        val removed = items.removeAt(index)
        items.add(index, action(removed))
        state.value = items
    }
}