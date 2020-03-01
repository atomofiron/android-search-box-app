package ru.atomofiron.regextool.screens.finder.model

import ru.atomofiron.regextool.R

enum class FinderItemType(val id: Int) {
    FIND(R.layout.item_field_result),
    CHARACTERS(R.layout.item_characters),
    CONFIGS(R.layout.item_config),
    TEST(R.layout.item_test),
    PROGRESS(R.layout.item_progress),
    RESULT(R.layout.item_finder_file)
}