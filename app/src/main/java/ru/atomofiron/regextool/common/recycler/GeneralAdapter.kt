package ru.atomofiron.regextool.common.recycler

import androidx.recyclerview.widget.RecyclerView

abstract class GeneralAdapter<H : GeneralHolder<D>, D : Any> : RecyclerView.Adapter<H>() {
    protected val items: MutableList<D> = ArrayList()

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: H, position: Int) {
        holder.bind(items[position], position)
    }

    fun setItems(items: List<D>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun setItem(item: D) {
        val index = items.indexOf(item)
        if (index != -1) {
            items[index] = item
            notifyItemChanged(index)
        }
    }
}