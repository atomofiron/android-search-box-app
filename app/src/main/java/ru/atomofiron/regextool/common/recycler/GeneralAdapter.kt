package ru.atomofiron.regextool.common.recycler

import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception

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

    fun removeItem(item: D) {
        val index = items.indexOf(item)
        if (index != -1) {
            items.remove(item)
            notifyItemRemoved(index)
        }
    }

    fun insertItem(previous: D, item: D) {
        val index = this.items.indexOf(previous).inc()
        this.items.add(index, item)
        notifyItemInserted(index)
    }

    fun removeItems(items: List<D>) {
        val firstIndex = this.items.indexOf(items.first())
        for (i in items.indices) {
            val removed = this.items.removeAt(firstIndex)
            require(removed == items[i]) { Exception("removeItems: $removed != ${items[i]}") }
        }
        notifyItemRangeRemoved(firstIndex, items.size)
    }

    fun insertItems(previous: D, items: List<D>) {
        val index = this.items.indexOf(previous).inc()
        this.items.addAll(index, items)
        notifyItemRangeInserted(index, items.size)
    }
}