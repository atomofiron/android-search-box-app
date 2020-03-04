package app.atomofiron.common.recycler

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
        var indexFirst = UNKNOWN
        var indexLast = UNKNOWN
        val lastCount = items.size.dec()

        for (i in items.indices) {
            if (indexFirst == UNKNOWN) {
                indexFirst = this.items.indexOf(items[i])
            }
            if (indexLast == UNKNOWN) {
                indexLast = this.items.indexOf(items[lastCount - i])
            }
        }

        if (indexFirst == UNKNOWN) {
            return
        }

        for (index in indexFirst..indexLast) {
            this.items.removeAt(indexFirst)
        }

        notifyItemRangeRemoved(indexFirst, indexLast.inc() - indexFirst)
    }

    fun insertItems(previous: D, items: List<D>) {
        val index = this.items.indexOf(previous).inc()
        this.items.addAll(index, items)
        notifyItemRangeInserted(index, items.size)
    }

    fun setItem(index: Int, item: D) {
        items[index] = item
        notifyItemChanged(index)
    }

    fun removeItem(index: Int) {
        items.removeAt(index)
        notifyItemRemoved(index)
    }

    fun insertItem(index: Int, item: D) {
        items.add(index, item)
        notifyItemInserted(index)
    }
}