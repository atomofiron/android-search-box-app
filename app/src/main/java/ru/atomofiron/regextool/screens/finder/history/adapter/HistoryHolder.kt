package ru.atomofiron.regextool.screens.finder.history.adapter

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R

class HistoryHolder(itemView: View, onItemActionListener: OnItemActionListener) : RecyclerView.ViewHolder(itemView) {
    private val btnPinned = itemView.findViewById<ImageButton>(R.id.item_history_btn_pinned)
    private val tvTitle = itemView.findViewById<TextView>(R.id.item_history_tv_title)
    private val btnRemove = itemView.findViewById<ImageButton>(R.id.item_history_btn_remove)

    init {
        itemView.setOnClickListener {
            onItemActionListener.onItemClick(adapterPosition)
        }
        btnPinned.setOnClickListener {
            onItemActionListener.onItemPin(adapterPosition)
        }
        btnRemove.setOnClickListener {
            onItemActionListener.onItemRemove(adapterPosition)
        }
    }

    fun onBind(title: String, pinned: Boolean) {
        btnPinned.isActivated = pinned
        tvTitle.text = title
        btnRemove.visibility = if (pinned) View.GONE else View.VISIBLE
    }

    interface OnItemActionListener {
        fun onItemRemove(position: Int)
        fun onItemClick(position: Int)
        fun onItemPin(position: Int)
    }
}