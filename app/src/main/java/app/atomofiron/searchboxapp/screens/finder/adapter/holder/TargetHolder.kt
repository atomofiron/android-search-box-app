package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

class TargetHolder(parent: ViewGroup, id: Int) : GeneralHolder<FinderStateItem>(parent, id) {
    private val ivIcon = itemView.findViewById<ImageView>(R.id.item_iv_icon)
    private val tvTitle = itemView.findViewById<TextView>(R.id.item_tv_title)

    init {
        itemView.isClickable = false
        itemView.isFocusable = false
    }

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.TargetItem

        val icon = when {
            !item.target.isDirectory -> R.drawable.ic_file_circle
            item.target.children?.isEmpty() == true -> R.drawable.ic_explorer_folder_empty
            else -> R.drawable.ic_explorer_folder
        }
        ivIcon.setImageResource(icon)
        ivIcon.alpha = if (item.target.isDirectory && !item.target.isCached) .4f else 1f
        tvTitle.text = item.target.path
    }
}