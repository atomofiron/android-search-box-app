package ru.atomofiron.regextool.screens.root.util.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import app.atomofiron.common.recycler.GeneralAdapter
import ru.atomofiron.regextool.R

class TasksAdapter : GeneralAdapter<TaskHolder, XTask>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): TaskHolder {
        val view = inflater.inflate(R.layout.item_task, parent, false)
        return TaskHolder(view)
    }
}