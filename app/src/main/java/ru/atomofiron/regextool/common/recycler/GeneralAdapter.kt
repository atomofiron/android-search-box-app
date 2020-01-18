package ru.atomofiron.regextool.common.recycler

import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.Util.log

abstract class GeneralAdapter<H : GeneralHolder<D>, D : Any> : RecyclerView.Adapter<H>() {
    protected val items: MutableList<D> = ArrayList()

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: H, position: Int) {
        holder.bind(items[position], position)
    }

    fun setItems(items: List<D>) {
        log("setItems ${items.size}")
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun notifyItemChanged(item: D) {
        log("notifyItemChanged $item")
        val index = items.indexOf(item)
        notifyItemChanged(index)
    }
}