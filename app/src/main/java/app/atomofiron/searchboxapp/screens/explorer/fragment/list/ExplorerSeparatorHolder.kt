package app.atomofiron.searchboxapp.screens.explorer.fragment.list

import android.view.View
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.databinding.ItemExplorerSeparatorBinding
import app.atomofiron.searchboxapp.model.explorer.Node

class ExplorerSeparatorHolder(itemView: View) : GeneralHolder<Node>(itemView) {

    private val binding = ItemExplorerSeparatorBinding.bind(itemView)
    private var clickListener: ((Node) -> Unit)? = null

    init {
        itemView.setOnClickListener {
            clickListener?.invoke(item)
        }
    }

    override fun onBind(item: Node, position: Int) {
        binding.itemExplorerTvTitle.text = item.name
    }

    fun setOnClickListener(listener: (Node) -> Unit) {
        clickListener = listener
    }
}