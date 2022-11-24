package app.atomofiron.searchboxapp.screens.explorer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeAction
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.adapter.util.NodeCallback
import lib.atomofiron.android_window_insets_compat.applyPaddingInsets
import java.util.LinkedList

class ExplorerAdapter : ListAdapter<Node, ExplorerHolder>(NodeCallback()) {
    companion object {
        private const val UNDEFINED = -1
        private const val VIEW_TYPE = 1
        private const val VIEW_POOL_MAX_COUNT = 30
    }

    lateinit var itemActionListener: ExplorerItemActionListener
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
        recyclerView.recycledViewPool.setMaxRecycledViews(VIEW_TYPE, VIEW_POOL_MAX_COUNT)

        viewPool = arrayOfNulls(VIEW_POOL_MAX_COUNT)
        val inflater = LayoutInflater.from(recyclerView.context)
        for (i in viewPool!!.indices) {
            viewPool!![i] = inflateNewView(inflater, recyclerView)
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun getItemViewType(position: Int): Int = VIEW_TYPE

    override fun getItemId(position: Int): Long = currentList[position].uniqueId.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExplorerHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = getNewView(inflater, parent)
        view.applyPaddingInsets(start = true, end = true)
        return ExplorerHolder(view)
    }

    override fun onBindViewHolder(holder: ExplorerHolder, position: Int) {
        val item = getItem(position)
        holder.onBind(item)
        holder.setOnItemActionListener(itemActionListener)
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
}