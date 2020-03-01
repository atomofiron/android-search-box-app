package ru.atomofiron.regextool.screens.finder.model

import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.iss.service.model.XFile

sealed class FinderStateItem(val stableId: Long, val layoutId: Int) {
    companion object {
        const val SEARCH_POSITION = 0
        const val CHARACTERS_POSITION = 1
        const val CONFIG_POSITION = 2
    }
    class SearchAndReplace(val replaceEnabled: Boolean = false)
        : FinderStateItem(SEARCH_POSITION.inc().toLong(), R.layout.layout_field_find)
    class SpecialCharacters(val characters: Array<String>)
        : FinderStateItem(CHARACTERS_POSITION.inc().toLong(), R.layout.layout_characters)
    class Config(val ignoreCase: Boolean = false,
                 val userRegexp: Boolean = false,
                 val searchInContent: Boolean = false,
                 val multilineSearch: Boolean = false,
                 val replaceEnabled: Boolean = false,
                 val configVisible: Boolean = false)
        : FinderStateItem(CONFIG_POSITION.inc().toLong(), R.layout.layout_config) {
        fun copy(
                ignoreCase: Boolean = this.ignoreCase,
                userRegexp: Boolean = this.userRegexp,
                searchInContent: Boolean = this.searchInContent,
                multilineSearch: Boolean = this.multilineSearch,
                searchAndReplace: Boolean = this.replaceEnabled,
                configVisible: Boolean = this.configVisible): Config {
            return Config(ignoreCase, userRegexp, searchInContent, multilineSearch, searchAndReplace, configVisible)
        }
    }
    class ProgressItem(val id: Long, val status: String)
        : FinderStateItem(id, R.layout.layout_test)
    class ResultItem(val id: Long, val target: XFile)
        : FinderStateItem(id, R.layout.layout_progress)
}