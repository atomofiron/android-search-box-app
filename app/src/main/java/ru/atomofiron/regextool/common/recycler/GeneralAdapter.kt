package ru.atomofiron.regextool.common.recycler

import androidx.recyclerview.widget.RecyclerView

abstract class GeneralAdapter<H : GeneralHolder<D>, D> : RecyclerView.Adapter<H>() {
    protected val items: MutableList<D> = ArrayList()

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: H, position: Int) {
        holder.onBind(items[position], position)
    }

    fun setItems(items: List<D>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }
}