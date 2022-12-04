package app.atomofiron.searchboxapp.screens.explorer.fragment.roots

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.explorer.NodeRoot

private class ItemCallbackImpl : DiffUtil.ItemCallback<NodeRoot>() {
    override fun areItemsTheSame(oldItem: NodeRoot, newItem: NodeRoot): Boolean {
        return oldItem::class == newItem::class
    }

    override fun areContentsTheSame(oldItem: NodeRoot, newItem: NodeRoot): Boolean {
        return oldItem == newItem
    }
}

class RootAdapter : ListAdapter<NodeRoot, RootViewHolder>(ItemCallbackImpl()) {

    init {
        setHasStableIds(true)
        val a = listOf(
            NodeRoot.NodeRootType.Photos,
            NodeRoot.NodeRootType.Videos,
            NodeRoot.NodeRootType.Downloads,
            NodeRoot.NodeRootType.Bluetooth,
            NodeRoot.NodeRootType.Screenshots,
            NodeRoot.NodeRootType.InternalStorage,
        ).map { NodeRoot(it) }
        submitList(a)
    }

    override fun getItemId(position: Int): Long = currentList[position].hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RootViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_explorer_card, parent, false)
        itemView.setOnClickListener { }
        return RootViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RootViewHolder, position: Int) {
        holder.bind(currentList[position], position)
    }
}