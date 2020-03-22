package ru.atomofiron.regextool.screens.finder

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import app.atomofiron.common.base.BaseViewModel
import app.atomofiron.common.util.LateinitLiveData
import app.atomofiron.common.util.SingleLiveEvent
import ru.atomofiron.regextool.channel.PreferencesChannel
import ru.atomofiron.regextool.di.DaggerInjector
import ru.atomofiron.regextool.iss.service.explorer.model.MutableXFile
import ru.atomofiron.regextool.iss.store.ExplorerStore
import ru.atomofiron.regextool.iss.store.SettingsStore
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem.*
import ru.atomofiron.regextool.screens.finder.model.FinderStateItemUpdate
import ru.atomofiron.regextool.screens.finder.model.FinderStateItemUpdate.*
import javax.inject.Inject
import kotlin.reflect.KClass


class FinderViewModel(app: Application) : BaseViewModel<FinderRouter>(app) {
    override val router = FinderRouter()

    private val items = ArrayList<FinderStateItem>()
    val historyDrawerGravity = MutableLiveData<Int>()
    val state = LateinitLiveData<List<FinderStateItem>>()
    val reloadHistory = SingleLiveEvent<Unit>()
    val insertInQuery = SingleLiveEvent<String>()
    val replaceQuery = SingleLiveEvent<String>()
    val updateContent = SingleLiveEvent<FinderStateItemUpdate>()

    private var configItem: ConfigItem? = ConfigItem()

    @Inject
    lateinit var explorerStore: ExplorerStore

    init {
        items.add(SearchAndReplaceItem())
        val characters = SettingsStore.specialCharacters.entity
        items.add(SpecialCharactersItem(characters))
        items.add(TestItem())
        items.add(ProgressItem(777, "9/36"))
        for (i in 1L..30L)
        items.add(ResultItem(i + 900L, MutableXFile("-rwxrwxrwx", "atomofiron", "atomofiron", "7B", "DATE", "TIME", "some_file", "", false, "/sdcard/search/path/some_file", 1337)))
        state.value = items
    }

    override fun buildComponentAndInject() {
        DaggerFinderComponent
                .builder()
                .dependencies(DaggerInjector.appComponent)
                .build()
                .inject(this)
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
        when (val configItem = configItem) {
            null -> {
                this.configItem = items.removeAt(FinderStateItem.CONFIG_POSITION) as ConfigItem
                updateContent.invoke(Removed(FinderStateItem.CONFIG_POSITION))
            }
            else -> {
                items.add(FinderStateItem.CONFIG_POSITION, configItem)
                updateContent.invoke(Inserted(FinderStateItem.CONFIG_POSITION, configItem))
                this.configItem = null
            }
        }
    }

    fun onSettingsOptionSelected() = router.showSettings()

    fun onHistoryItemClick(node: String) = replaceQuery.invoke(node)

    private fun <I : FinderStateItem> updateItem(item: I, klass: KClass<I>) {
        val index = items.indexOfFirst { it::class == klass }
        items.removeAt(index)
        items.add(index, item)
        updateContent.invoke(Changed(index, item))
    }

    private fun <I : FinderStateItem> updateItem(klass: KClass<I>, action: (I) -> I) {
        val index = items.indexOfFirst { it::class == klass }
        val removed = items.removeAt(index)
        @Suppress("UNCHECKED_CAST")
        val item = action(removed as I)
        items.add(index, item)
        updateContent.invoke(Changed(index, item))
    }
}