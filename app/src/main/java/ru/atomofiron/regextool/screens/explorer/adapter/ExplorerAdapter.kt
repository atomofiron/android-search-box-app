package ru.atomofiron.regextool.screens.explorer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.recycler.GeneralAdapter
import ru.atomofiron.regextool.iss.service.model.XFile

class ExplorerAdapter : GeneralAdapter<ExplorerHolder, XFile>() {
    companion object {
        private const val VIEW_TYPE = 1
        private const val VIEW_POOL_MAX_COUNT = 20
    }

    private var onItemClickListener: ((position: Int) -> Unit)? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.itemAnimator = null
        recyclerView.recycledViewPool.setMaxRecycledViews(VIEW_TYPE, VIEW_POOL_MAX_COUNT)
    }

    override fun getItemViewType(position: Int): Int = VIEW_TYPE

    override fun getItemId(position: Int): Long = items[position].hashCode().toLong()

    fun setOnItemClickListener(listener: ((position: Int) -> Unit)?) {
        onItemClickListener = listener
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExplorerHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_explorer_file, parent, false)
        return ExplorerHolder(view)
    }

    override fun onBindViewHolder(holder: ExplorerHolder, position: Int) {
        holder.onItemClickListener = onItemClickListener
        super.onBindViewHolder(holder, position)
    }
}