package app.atomofiron.searchboxapp.screens.finder.model

import androidx.annotation.StringRes
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.FinderTask

sealed class FinderStateItem(val stableId: Long, val layoutId: Int) {
    companion object {
        private const val SEARCH_ID = 1L
        private const val CHARACTERS_ID = 2L
        private const val CONFIG_ID = 3L
        private const val TEST_ID = 4L
    }
    data class SearchAndReplaceItem(
        var query: String = "", // mutable field
        val replaceEnabled: Boolean = false,
        val useRegex: Boolean = false,
    ) : FinderStateItem(SEARCH_ID, R.layout.item_field_search)

    class SpecialCharactersItem(
        val characters: Array<String>,
    ) : FinderStateItem(CHARACTERS_ID, R.layout.item_characters)

    data class ConfigItem(
      val ignoreCase: Boolean = false,
      val useRegex: Boolean = false,
      val searchInContent: Boolean = false,
      val excludeDirs: Boolean = false,
      val replaceEnabled: Boolean = false,
      val isLocal: Boolean = false,
    ) : FinderStateItem(CONFIG_ID, R.layout.item_config)

    data class TestItem(
        val searchQuery: String = "",
        val useRegex: Boolean = false,
        val ignoreCase: Boolean = true,
    ) : FinderStateItem(TEST_ID, R.layout.item_test)

    class ProgressItem(
        val finderTask: FinderTask,
    ) : FinderStateItem(finderTask.id, R.layout.item_progress)

    class TargetItem(
        val target: Node,
    ) : FinderStateItem(target.hashCode().toLong(), R.layout.item_finder_target)

    class TipItem(
        @StringRes val titleId: Int,
    ) : FinderStateItem(titleId.hashCode().toLong(), R.layout.item_finder_tip)

    override fun equals(other: Any?): Boolean = when (other) {
        !is FinderStateItem -> false
        is SpecialCharactersItem -> when (this) {
            is SpecialCharactersItem -> other.characters.contentEquals(characters)
            else -> false
        }
        else -> other.stableId == stableId
    }

    override fun hashCode(): Int = when (this) {
        is SearchAndReplaceItem -> stableId.toInt()
        is SpecialCharactersItem -> characters.hashCode()
        is ConfigItem -> stableId.toInt()
        is TestItem -> stableId.toInt()
        is ProgressItem -> finderTask.hashCode()
        is TargetItem -> stableId.toInt()
        is TipItem -> stableId.toInt()
    }
}