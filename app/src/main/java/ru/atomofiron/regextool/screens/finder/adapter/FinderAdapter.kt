package ru.atomofiron.regextool.screens.finder.adapter

import android.view.ViewGroup
import ru.atomofiron.regextool.common.recycler.GeneralAdapter
import ru.atomofiron.regextool.common.recycler.GeneralHolder
import ru.atomofiron.regextool.screens.finder.adapter.holder.*

class FinderAdapter : GeneralAdapter<GeneralHolder<FinderItem>, FinderItem>() {
    override fun onBindViewHolder(holder: GeneralHolder<FinderItem>, position: Int) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeneralHolder<FinderItem> {
        return when (viewType) {
            FinderItemType.REPLACE.id -> FieldHolder(parent, viewType)
            FinderItemType.FIND.id -> FieldHolder(parent, viewType)
            FinderItemType.CHARACTERS.id -> CharactersHolder(parent, viewType)
            FinderItemType.CONFIGS.id -> ConfigHolder(parent, viewType)
            FinderItemType.TEST.id -> TestHolder(parent, viewType)
            FinderItemType.PROGRESS.id -> ProgressHolder(parent, viewType)
            FinderItemType.FILE.id -> FileHolder(parent, viewType)
            else -> throw IllegalArgumentException("viewType = $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int = items[position].id
}