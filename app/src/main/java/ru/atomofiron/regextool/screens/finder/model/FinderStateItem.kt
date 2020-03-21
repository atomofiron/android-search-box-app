package ru.atomofiron.regextool.screens.finder.model

import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.iss.service.explorer.model.XFile

sealed class FinderStateItem(val stableId: Long, val layoutId: Int) {
    companion object {
        const val SEARCH_POSITION = 0
        const val CHARACTERS_POSITION = 1
        const val CONFIG_POSITION = 2
        const val TEST_POSITION = 3
    }
    class SearchAndReplaceItem(val replaceEnabled: Boolean = false,
                               val useRegexp: Boolean = false)
        : FinderStateItem(SEARCH_POSITION.inc().toLong(), R.layout.item_field_search)
    class SpecialCharactersItem(val characters: Array<String>)
        : FinderStateItem(CHARACTERS_POSITION.inc().toLong(), R.layout.item_characters)
    class ConfigItem(val ignoreCase: Boolean = false,
                     val useRegexp: Boolean = false,
                     val searchInContent: Boolean = false,
                     val multilineSearch: Boolean = false,
                     val replaceEnabled: Boolean = false)
        : FinderStateItem(CONFIG_POSITION.inc().toLong(), R.layout.item_config) {
        fun copy(
                ignoreCase: Boolean = this.ignoreCase,
                useRegexp: Boolean = this.useRegexp,
                searchInContent: Boolean = this.searchInContent,
                multilineSearch: Boolean = this.multilineSearch,
                searchAndReplace: Boolean = this.replaceEnabled): ConfigItem {
            return ConfigItem(ignoreCase, useRegexp, searchInContent, multilineSearch, searchAndReplace)
        }
    }
    class TestItem(val searchQuery: String = "",
                   val useRegex: Boolean = false,
                   val ignoreCase: Boolean = true,
                   val multilineSearch: Boolean = false)
        : FinderStateItem(TEST_POSITION.inc().toLong(), R.layout.item_test) {
        fun copy(searchQuery: String = this.searchQuery,
                 useRegexp: Boolean = this.useRegex,
                 ignoreCase: Boolean = this.ignoreCase,
                 multilineSearch: Boolean = this.multilineSearch): TestItem {
            return TestItem(searchQuery, useRegexp, ignoreCase, multilineSearch)
        }
    }
    class ProgressItem(val id: Long, val status: String)
        : FinderStateItem(id, R.layout.item_progress)
    class ResultItem(val id: Long, val target: XFile)
        : FinderStateItem(id, R.layout.item_finder_result)
}