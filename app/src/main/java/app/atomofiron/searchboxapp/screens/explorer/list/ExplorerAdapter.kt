package app.atomofiron.searchboxapp.screens.explorer.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView.Companion.makeOpened
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView.Companion.makeOpenedCurrent
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView.Companion.makeSeparator
import app.atomofiron.searchboxapp.databinding.ItemExplorerBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeAction
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.list.util.NodeCallback
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.isDot
import java.util.LinkedList

class ExplorerAdapter : ListAdapter<Node, ExplorerHolder>(NodeCallback()) {
    companion object {
        private const val VIEW_TYPE_ANY = 1
        private const val VIEW_TYPE_OPENED = 2
        private const val VIEW_TYPE_OPENED_CURRENT = 3
        private const val VIEW_TYPE_OPENED_SEPARATOR = 4
        private const val VIEW_POOL_MAX_COUNT = 30
    }

    lateinit var itemActionListener: ExplorerItemActionListener
    lateinit var separatorClickListener: (Node) -> Unit
    private val separatorListener = SeparatorItemActionListener()
    private var viewPool: Array<View?>? = null

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
        recyclerView.recycledViewPool.setMaxRecycledViews(VIEW_TYPE_ANY, VIEW_POOL_MAX_COUNT)

        viewPool = arrayOfNulls(VIEW_POOL_MAX_COUNT)
        val inflater = LayoutInflater.from(recyclerView.context)
        for (i in viewPool!!.indices) {
            viewPool!![i] = inflateNewView(inflater, recyclerView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = currentList[position]
        return when {
            item.isDot() -> VIEW_TYPE_OPENED_SEPARATOR
            item.isCurrent -> VIEW_TYPE_OPENED_CURRENT
            item.isOpened -> VIEW_TYPE_OPENED
            else -> VIEW_TYPE_ANY
        }
    }

    override fun getItemId(position: Int): Long = currentList[position].uniqueId.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExplorerHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = getNewView(inflater, parent)
        when (viewType) {
            VIEW_TYPE_OPENED -> ItemExplorerBinding.bind(view).makeOpened()
            VIEW_TYPE_OPENED_CURRENT -> ItemExplorerBinding.bind(view).makeOpenedCurrent()
            VIEW_TYPE_OPENED_SEPARATOR -> ItemExplorerBinding.bind(view).makeSeparator()
        }
        return ExplorerHolder(view)
    }

    override fun onBindViewHolder(holder: ExplorerHolder, position: Int) {
        val item = getItem(position)
        holder.onBind(item)
        when {
            item.isDot() -> holder.setOnItemActionListener(separatorListener)
            else -> holder.setOnItemActionListener(itemActionListener)
        }
        holder.bindComposition(composition)
    }

    override fun onViewAttachedToWindow(holder: ExplorerHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder.bindingAdapterPosition > 0) {
            itemActionListener.onItemVisible(getItem(holder.bindingAdapterPosition))
        }
    }

    private val actions = LinkedList<NodeAction>()

    fun onAction(action: NodeAction) {
        actions.add(action)
    }

    private fun getNewView(inflater: LayoutInflater, parent: ViewGroup): View {
        val viewPool = viewPool
        if (viewPool != null) {
            for (i in viewPool.indices) {
                val view = viewPool[i]
                if (view != null) {
                    viewPool[i] = null
                    return view
                }
            }
            this.viewPool = null
        }
        return inflateNewView(inflater, parent)
    }

    private fun inflateNewView(inflater: LayoutInflater, parent: ViewGroup): View {
        return inflater.inflate(R.layout.item_explorer, parent, false)
    }

    private inner class SeparatorItemActionListener : ExplorerItemActionListener {
        override fun onItemVisible(item: Node) = Unit
        override fun onItemLongClick(item: Node) = Unit
        override fun onItemCheck(item: Node, isChecked: Boolean) = Unit
        override fun onItemClick(item: Node) = separatorClickListener(item)
    }
}