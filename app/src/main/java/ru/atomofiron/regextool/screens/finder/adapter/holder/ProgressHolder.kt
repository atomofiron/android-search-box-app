package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import app.atomofiron.common.util.findColorByAttr
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.BallsView
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem
import ru.atomofiron.regextool.utils.setVisibility

class ProgressHolder(parent: ViewGroup, id: Int, listener: OnActionListener) : CardViewHolder(parent, id) {
    private val tvLabel = itemView.findViewById<TextView>(R.id.progress_tv_label)
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
        val text = when {
            task.isSecondary -> task.count.toString()
            else -> "${task.results.size}/${task.count}"
        }
        tvStatus.text = text
        btnAction.isActivated = !task.inProgress
        btnAction.isEnabled = true
        bView.setVisibility(task.inProgress, View.INVISIBLE)

        val idLabel = when {
            task.inProgress -> R.string.look
            task.error != null -> R.string.error
            task.isDone -> R.string.done
            else -> R.string.stopped
        }
        val colorLabel = when {
            task.inProgress -> context.findColorByAttr(R.attr.colorAccent)
            task.error != null -> ContextCompat.getColor(context, R.color.colorAccentRed)
            task.isDone -> context.findColorByAttr(R.attr.colorAccent)
            else ->  ContextCompat.getColor(context, R.color.colorAccentYellow)
        }
        tvLabel.setText(idLabel)
        tvLabel.setTextColor(colorLabel)

        val idAction = when {
            task.inProgress -> R.string.stop
            else -> R.string.remove
        }
        btnAction.setText(idAction)
        btnAction.setVisibility(task.isDone && task.isRemovable)
    }

    interface OnActionListener {
        fun onItemClick(item: FinderStateItem.ProgressItem)
        fun onProgressStopClick(item: FinderStateItem.ProgressItem)
        fun onProgressRemoveClick(item: FinderStateItem.ProgressItem)
    }
}