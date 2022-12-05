package app.atomofiron.searchboxapp.screens.explorer.fragment.list

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeAction
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerHolder
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerItemViewFactory
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerSeparatorHolder
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.NodeCallback
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.isDot
import java.util.LinkedList

class ExplorerAdapter(
    private val rootAliases: Map<Int, String>,
) : ListAdapter<Node, GeneralHolder<Node>>(NodeCallback()) {

    lateinit var itemActionListener: ExplorerItemActionListener
    lateinit var separatorClickListener: (Node) -> Unit

    private lateinit var composition: ExplorerItemComposition

    init {
        setHasStableIds(true)
    }

    fun setComposition(composition: ExplorerItemComposition) {
        this.composition = composition
        notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.itemAnimator = null
    }

    override fun getItemViewType(position: Int): Int {
        val item = currentList[position]
        return when {
            item.isDot() -> ExplorerItemViewFactory.SeparatorNodeItem.ordinal
            item.isCurrent -> ExplorerItemViewFactory.CurrentOpenedNodeItem.ordinal
            item.isOpened -> ExplorerItemViewFactory.OpenedNodeItem.ordinal
            else -> ExplorerItemViewFactory.NodeItem.ordinal
        }
    }

    override fun getItemId(position: Int): Long = currentList[position].uniqueId.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeneralHolder<Node> {
        return ExplorerItemViewFactory.values()[viewType].createHolder(parent, rootAliases)
    }

    override fun onBindViewHolder(holder: GeneralHolder<Node>, position: Int) {
        val item = getItem(position)
        holder.bind(item, position)
        when (holder) {
            is ExplorerSeparatorHolder -> holder.setOnClickListener(separatorClickListener)
            is ExplorerHolder -> {
                holder.setOnItemActionListener(itemActionListener)
                holder.bindComposition(composition)
            }
        }
    }

    override fun onViewAttachedToWindow(holder: GeneralHolder<Node>) {
        super.onViewAttachedToWindow(holder)
        if (holder.bindingAdapterPosition > 0) {
            itemActionListener.onItemVisible(getItem(holder.bindingAdapterPosition))
        }
    }

    private val actions = LinkedList<NodeAction>()

    fun onAction(action: NodeAction) {
        actions.add(action)
    }
}