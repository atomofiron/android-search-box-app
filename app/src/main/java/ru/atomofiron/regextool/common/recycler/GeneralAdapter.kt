package ru.atomofiron.regextool.common.recycler

import androidx.recyclerview.widget.RecyclerView

abstract class GeneralAdapter<H : GeneralHolder<D>, D : Any> : RecyclerView.Adapter<H>() {
    companion object {
        private const val UNKNOWN = -1
    }
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
        if (index != UNKNOWN) {
            items[index] = item
            notifyItemChanged(index)
        }
    }

    fun removeItem(item: D) {
        val index = items.indexOf(item)
        if (index != UNKNOWN) {
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
        var firstIndex = UNKNOWN
        var lastIndex = UNKNOWN
        // вместо offset лучше удалять с конца
        var offset = 0
        items.forEachIndexed { forIndex, it ->
            val index = this.items.indexOf(it)
            if (index != UNKNOWN) {
                lastIndex = index + offset
                if (firstIndex == UNKNOWN) {
                    firstIndex = index + offset
                }
                this.items.removeAt(index)
                offset++
            }
            val theLastIteration = forIndex == items.size.dec()
            if (index == UNKNOWN || theLastIteration) {
                if (firstIndex != UNKNOWN) {
                    notifyItemRangeRemoved(firstIndex, lastIndex - firstIndex.inc())
                }
            }
        }
    }

    fun insertItems(previous: D, items: List<D>) {
        val index = this.items.indexOf(previous).inc()
        this.items.addAll(index, items)
        notifyItemRangeInserted(index, items.size)
    }
}