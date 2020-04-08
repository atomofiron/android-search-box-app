package ru.atomofiron.regextool.screens.finder.model

import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.injectable.service.explorer.model.XFile

sealed class FinderStateItem(val stableId: Long, val layoutId: Int) {
    companion object {
        private const val SEARCH_ID = 0L + 1L
        private const val CHARACTERS_ID = 1L + 1L
        private const val CONFIG_ID = 2L + 1L
        private const val TEST_ID = 3L + 1L
    }
    data class SearchAndReplaceItem(var query: String = "", // mutable field
                                    val replaceEnabled: Boolean = false,
                                    val useRegex: Boolean = false)
        : FinderStateItem(SEARCH_ID, R.layout.item_field_search)
    class SpecialCharactersItem(val characters: Array<String>)
        : FinderStateItem(CHARACTERS_ID, R.layout.item_characters)
    data class ConfigItem(val ignoreCase: Boolean = false,
                     val useRegex: Boolean = false,
                     val searchInContent: Boolean = false,
                     val multilineSearch: Boolean = false,
                     val replaceEnabled: Boolean = false)
        : FinderStateItem(CONFIG_ID, R.layout.item_config)
    data class TestItem(val searchQuery: String = "",
                   val useRegex: Boolean = false,
                   val ignoreCase: Boolean = true,
                   val multilineSearch: Boolean = false)
        : FinderStateItem(TEST_ID, R.layout.item_test)
    class ProgressItem(val id: Long, val status: String)
        : FinderStateItem(id, R.layout.item_progress)
    class TargetItem(val target: XFile)
        : FinderStateItem(target.hashCode().toLong(), R.layout.item_finder_target)
}