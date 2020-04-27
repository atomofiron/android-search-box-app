package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.BallsView
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem

class ProgressHolder(parent: ViewGroup, id: Int, listener: OnActionListener) : CardViewHolder(parent, id) {
    private val tvStatus = itemView.findViewById<TextView>(R.id.progress_tv_counter)
    private val bView = itemView.findViewById<BallsView>(R.id.item_explorer_ps)
    private val btnAction = itemView.findViewById<Button>(R.id.progress_btn_action)

    init {
        itemView.setOnClickListener {
            listener.onItemClick(item as FinderStateItem.ProgressItem)
        }
        btnAction.setOnClickListener { view ->
            view.isEnabled = false

            val item = item as FinderStateItem.ProgressItem
            if (item.finderTask.inProgress)
                listener.onProgressStopClick(item)
            else
                listener.onProgressRemoveClick(item)
        }
    }

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.ProgressItem
        val task = item.finderTask
        val text = "${task.results.size}/${task.count}"
        tvStatus.text = text
        btnAction.isActivated = !task.inProgress
        btnAction.isEnabled = true

        when (task.inProgress) {
            true -> {
                bView.visibility = View.VISIBLE
                btnAction.text = btnAction.resources.getString(R.string.stop)
            }
            else -> {
                bView.visibility = View.INVISIBLE
                btnAction.text = btnAction.resources.getString(R.string.remove)
            }
        }
    }

    interface OnActionListener {
        fun onItemClick(item: FinderStateItem.ProgressItem)
        fun onProgressStopClick(item: FinderStateItem.ProgressItem)
        fun onProgressRemoveClick(item: FinderStateItem.ProgressItem)
    }
}