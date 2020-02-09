package ru.atomofiron.regextool.screens.finder.adapter

import android.view.ViewGroup
import ru.atomofiron.regextool.common.recycler.GeneralAdapter
import ru.atomofiron.regextool.common.recycler.GeneralHolder
import ru.atomofiron.regextool.screens.finder.adapter.holder.*
import ru.atomofiron.regextool.screens.finder.adapter.item.FinderItem
import ru.atomofiron.regextool.screens.finder.adapter.item.FinderItemType

class FinderAdapter(
        private val onFinderActionListener: OnFinderActionListener
) : GeneralAdapter<GeneralHolder<FinderItem>, FinderItem>() {
    private val onSearchClickListener: (String) -> Unit = onFinderActionListener::onSearchClick

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeneralHolder<FinderItem> {
        return when (viewType) {
            FinderItemType.FIND.id -> FieldHolder(parent, viewType, onSearchClickListener)
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