package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem

class TargetHolder(parent: ViewGroup, id: Int, listener: OnActionListener) : CardViewHolder(parent, id) {
    private val ivIcon = itemView.findViewById<ImageView>(R.id.item_iv_icon)
    private val tvTitle = itemView.findViewById<TextView>(R.id.item_tv_title)

    init {
        itemView.setOnClickListener {
            listener.onItemClick(item as FinderStateItem.TargetItem)
        }
    }

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.TargetItem

        val icon = when {
            !item.target.isDirectory -> R.drawable.ic_file_circle
            item.target.files?.isEmpty() == true -> R.drawable.ic_explorer_folder_empty
            else -> R.drawable.ic_explorer_folder
        }
        ivIcon.setImageResource(icon)
        ivIcon.alpha = if (item.target.isDirectory && !item.target.isCached) .4f else 1f
        tvTitle.text = item.target.completedPath
    }

    interface OnActionListener {
        fun onItemClick(item: FinderStateItem.TargetItem)
    }
}