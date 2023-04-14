package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.BallsView
import app.atomofiron.searchboxapp.model.textviewer.SearchTask
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

class ProgressHolder(parent: ViewGroup, layoutId: Int, listener: OnActionListener) : CardViewHolder(parent, layoutId) {
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
            if (item.task.inProgress)
                listener.onProgressStopClick(item)
            else
                listener.onProgressRemoveClick(item)
        }
    }

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.ProgressItem
        val task = item.task
        tvStatus.setStatus(task)
        btnAction.isActivated = !task.inProgress
        btnAction.isEnabled = true
        bView.isInvisible = !task.inProgress

        val idLabel = when (task) {
            is SearchTask.Progress -> R.string.look
            is SearchTask.Error -> R.string.error
            is SearchTask.Done -> if (task.isCompleted) R.string.done else R.string.stopped
        }
        val colorLabel = when (task) {
            is SearchTask.Progress,
            is SearchTask.Done -> context.findColorByAttr(R.attr.colorAccent)
            is SearchTask.Error -> context.findColorByAttr(R.attr.colorError)
        }
        tvLabel.setText(idLabel)
        tvLabel.setTextColor(colorLabel)

        val idAction = when {
            task.inProgress -> R.string.stop
            else -> R.string.remove
        }
        btnAction.setText(idAction)
        btnAction.isGone = task.isDone && !task.isRemovable
    }

    private fun TextView.setStatus(task: SearchTask) {
        val counters = task.result.getCounters()
        val status = SpannableStringBuilder(" ")
        counters.reverse()
        counters.forEachIndexed { index, it ->
            status.insert(0, " *$it")
            val resId = when (index) {
                0 -> R.drawable.ic_status_file_all
                1 -> R.drawable.ic_status_file_found
                else -> R.drawable.ic_status_entry
            }
            status.setSpan(ImageSpan(context, resId, ImageSpan.ALIGN_BASELINE), 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        text = status
    }

    interface OnActionListener {
        fun onItemClick(item: FinderStateItem.ProgressItem)
        fun onProgressStopClick(item: FinderStateItem.ProgressItem)
        fun onProgressRemoveClick(item: FinderStateItem.ProgressItem)
    }
}