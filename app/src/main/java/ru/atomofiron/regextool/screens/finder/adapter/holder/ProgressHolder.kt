package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem

class ProgressHolder(parent: ViewGroup, id: Int, listener: OnActionListener) : CardViewHolder(parent, id) {
    private val tvStatus = itemView.findViewById<TextView>(R.id.progress_tv_counter)
    private val btnStop = itemView.findViewById<Button>(R.id.progress_btn_stop)

    init {
        itemView.setOnClickListener {
            listener.onItemClick(item as FinderStateItem.ProgressItem)
        }
        btnStop.setOnClickListener {
            listener.onProgressStopClick(item as FinderStateItem.ProgressItem)
        }
    }

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.ProgressItem
        tvStatus.text = item.status
    }

    interface OnActionListener {
        fun onItemClick(item: FinderStateItem.ProgressItem)
        fun onProgressStopClick(item: FinderStateItem.ProgressItem)
    }
}