package ru.atomofiron.regextool.screens.finder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.common.recycler.GeneralHolder
import ru.atomofiron.regextool.screens.finder.adapter.holder.*
import ru.atomofiron.regextool.screens.finder.model.FinderItemType
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem

class FinderAdapter : GeneralAdapter<GeneralHolder<FinderStateItem>, FinderStateItem>() {
    lateinit var output: FinderAdapterOutput

    init {
        setHasStableIds(true)
    }

    override fun setItems(new: List<FinderStateItem>) {
        val old = ArrayList<FinderStateItem>()
        old.addAll(items)
        items.clear()
        items.addAll(new)
        val callback = FinderDiffUtilCallback(old, new)
        val util = DiffUtil.calculateDiff(callback, false)
        util.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): GeneralHolder<FinderStateItem> {
        return when (viewType) {
            FinderItemType.FIND.id -> FieldHolder(parent, viewType, output)
            FinderItemType.CHARACTERS.id -> CharactersHolder(parent, viewType, output)
            FinderItemType.CONFIGS.id -> ConfigHolder(parent, viewType, output)
            FinderItemType.TEST.id -> TestHolder(parent, viewType)
            FinderItemType.PROGRESS.id -> ProgressHolder(parent, viewType, output)
            FinderItemType.TARGET.id -> TargetHolder(parent, viewType, output)
            else -> throw IllegalArgumentException("viewType = $viewType")
        }
    }

    override fun getItemId(position: Int): Long = items[position].stableId

    override fun getItemViewType(position: Int): Int = items[position].layoutId
}