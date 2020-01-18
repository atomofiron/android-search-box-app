package ru.atomofiron.regextool.screens.explorer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.recycler.GeneralAdapter
import ru.atomofiron.regextool.iss.service.model.XFile
import ru.atomofiron.regextool.log

class ExplorerAdapter : GeneralAdapter<ExplorerHolder, XFile>() {
    companion object {
        private const val VIEW_TYPE = 1
        private const val VIEW_POOL_MAX_COUNT = 30
    }

    private var onItemActionListener: ItemActionListener? = null
    private var viewPool: Array<View?>? = null

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

    override fun getItemViewType(position: Int): Int = VIEW_TYPE

    override fun getItemId(position: Int): Long = items[position].hashCode().toLong()

    fun setOnItemActionListener(listener: ItemActionListener?) {
        onItemActionListener = listener
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExplorerHolder {
        log("onCreateViewHolder")
        val inflater = LayoutInflater.from(parent.context)
        val view = getNewView(inflater, parent)

        return ExplorerHolder(view).apply {
            val rv = parent as RecyclerView
            itemView.setOnLongClickListener {
                log("pool ${rv.recycledViewPool.getRecycledViewCount(VIEW_TYPE)}")
                true
            }
        }
    }

    override fun onBindViewHolder(holder: ExplorerHolder, position: Int) {
        log("onBind")
        holder.onItemActionListener = onItemActionListener
        super.onBindViewHolder(holder, position)
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
        return inflater.inflate(R.layout.item_explorer_file, parent, false)
    }
}