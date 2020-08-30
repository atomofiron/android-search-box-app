package app.atomofiron.searchboxapp.screens.finder.model

import app.atomofiron.searchboxapp.R

enum class FinderItemType(val id: Int) {
    FIND(R.layout.item_field_search),
    CHARACTERS(R.layout.item_characters),
    CONFIGS(R.layout.item_config),
    TEST(R.layout.item_test),
    PROGRESS(R.layout.item_progress),
    TARGET(R.layout.item_finder_target),
    TIP(R.layout.item_finder_tip)
}