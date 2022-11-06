package app.atomofiron.searchboxapp.screens.explorer.adapter.util

import androidx.recyclerview.widget.DiffUtil
import app.atomofiron.searchboxapp.model.explorer.Node

class NodeCallback : DiffUtil.ItemCallback<Node>() {

    override fun areItemsTheSame(oldItem: Node, newItem: Node): Boolean = newItem.uniqueId == oldItem.uniqueId

    override fun areContentsTheSame(oldItem: Node, newItem: Node): Boolean = newItem.areContentsTheSame(oldItem)
}