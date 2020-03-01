package ru.atomofiron.regextool.screens.finder.adapter

import android.view.ViewGroup
import ru.atomofiron.regextool.common.recycler.GeneralAdapter
import ru.atomofiron.regextool.common.recycler.GeneralHolder
import ru.atomofiron.regextool.screens.finder.adapter.holder.*
import ru.atomofiron.regextool.screens.finder.model.FinderItemType
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem

class FinderAdapter : GeneralAdapter<GeneralHolder<FinderStateItem>, FinderStateItem>() {
    lateinit var onFinderActionListener: OnActionListener

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeneralHolder<FinderStateItem> {
        return when (viewType) {
            FinderItemType.FIND.id -> FieldHolder(parent, viewType, onFinderActionListener)
            FinderItemType.CHARACTERS.id -> CharactersHolder(parent, viewType, onFinderActionListener)
            FinderItemType.CONFIGS.id -> ConfigHolder(parent, viewType, onFinderActionListener)
            FinderItemType.TEST.id -> TestHolder(parent, viewType)
            FinderItemType.PROGRESS.id -> ProgressHolder(parent, viewType, onFinderActionListener)
            FinderItemType.RESULT.id -> ResultHolder(parent, viewType, onFinderActionListener)
            else -> throw IllegalArgumentException("viewType = $viewType")
        }
    }

    override fun getItemId(position: Int): Long = items[position].stableId

    override fun getItemViewType(position: Int): Int = items[position].layoutId

    interface OnActionListener :
            FieldHolder.OnActionListener,
            CharactersHolder.OnActionListener,
            ConfigHolder.OnActionListener,
            ProgressHolder.OnActionListener,
            ResultHolder.OnActionListener
}