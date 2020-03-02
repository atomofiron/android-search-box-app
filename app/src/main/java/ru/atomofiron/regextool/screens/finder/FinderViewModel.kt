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
import kotlin.reflect.KClass

class FinderViewModel(app: Application) : BaseViewModel<FinderRouter>(app) {
    override val router = FinderRouter()
    val historyDrawerGravity = MutableLiveData<Int>()
    val state = ReadyLiveData<List<FinderStateItem>>()
    val reloadHistory = SingleLiveEvent<Unit>()
    val insertInQuery = SingleLiveEvent<String>()

    private var configItem: ConfigItem? = ConfigItem()

    init {
        val items = ArrayList<FinderStateItem>()
        items.add(SearchAndReplaceItem())
        val characters = SettingsStore.specialCharacters.entity
        items.add(SpecialCharactersItem(characters))
        items.add(TestItem())
        items.add(ProgressItem(777, "9/36"))
        for (i in 1L..30L)
        items.add(ResultItem(i + 900L, MutableXFile("-rwxrwxrwx", "atomofiron", "atomofiron", "7B", "DATE", "TIME", "some_file", "", false, "/sdcard/search/path/some_file")))
        state.value = items
    }

    override fun onCreate(context: Context, intent: Intent) {
        super.onCreate(context, intent)

        SettingsStore
                .dockGravity
                .addObserver(onClearedCallback) { gravity ->
                    historyDrawerGravity.value = gravity
                }
        SettingsStore.specialCharacters.addObserver(onClearedCallback) { chs ->
            updateItem(SpecialCharactersItem(chs), SpecialCharactersItem::class)
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
            updateItem(it, SearchAndReplaceItem::class)
        }

        updateItem(newItem, ConfigItem::class)

        if (ignoreCaseChanged || replaceEnabledChanged || useRegexpChanged) {
            updateItem(TestItem::class) {
                it.copy(useRegexp = newItem.useRegexp,
                        ignoreCase = newItem.ignoreCase,
                        multilineSearch = newItem.multilineSearch)
            }
        }
    }

    fun onCharacterClick(value: String) = insertInQuery.invoke(value)

    fun onSearchChange(value: String) {
        updateItem(TestItem::class) {
            it.copy(searchQuery = value)
        }
    }

    fun onDockGravityChange(gravity: Int) = SettingsStore.dockGravity.push(gravity)

    fun onExplorerOptionSelected() {
        router.showExplorer()
    }

    fun onConfigOptionSelected() {
        val configItem = configItem
        val items = ArrayList<FinderStateItem>(state.value)
        when (configItem) {
            null -> this.configItem = items.removeAt(FinderStateItem.CONFIG_POSITION) as ConfigItem
            else -> {
                items.add(FinderStateItem.CONFIG_POSITION, configItem)
                this.configItem = null
            }
        }
        state.value = items
    }

    fun onSettingsOptionSelected() = router.showSettings()

    private fun <I : FinderStateItem> updateItem(item: I, klass: KClass<I>) {
        val items = ArrayList<FinderStateItem>(state.value)
        val index = items.indexOfFirst { it::class == klass }
        items.removeAt(index)
        items.add(index, item)
        state.value = items
    }

    private fun <I : FinderStateItem> updateItem(klass: KClass<I>, action: (I) -> I) {
        val items = ArrayList<FinderStateItem>(state.value)
        val index = items.indexOfFirst { it::class == klass }
        val removed = items.removeAt(index)
        @Suppress("UNCHECKED_CAST")
        items.add(index, action(removed as I))
        state.value = items
    }
}