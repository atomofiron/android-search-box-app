package ru.atomofiron.regextool.common.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

open class GeneralHolder<D>(view: View) : RecyclerView.ViewHolder(view) {
    constructor(parent: ViewGroup, id: Int)
            : this(LayoutInflater.from(parent.context).inflate(id, parent, false))

    open fun onBind(item: D, position: Int) = Unit
}