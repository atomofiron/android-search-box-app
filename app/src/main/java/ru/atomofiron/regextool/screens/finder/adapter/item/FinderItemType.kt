package ru.atomofiron.regextool.screens.finder.adapter.item

import ru.atomofiron.regextool.R

enum class FinderItemType(val id: Int) {
    FIND(R.layout.layout_field_find),
    CHARACTERS(R.layout.layout_characters),
    CONFIGS(R.layout.layout_config),
    TEST(R.layout.layout_test),
    PROGRESS(R.layout.layout_progress),
    FILE(R.layout.item_finder_file)
}