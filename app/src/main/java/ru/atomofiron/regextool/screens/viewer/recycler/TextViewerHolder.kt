package ru.atomofiron.regextool.screens.viewer.recycler

import android.widget.TextView
import app.atomofiron.common.recycler.GeneralHolder

class TextViewerHolder(val view: TextView) : GeneralHolder<String>(view) {
    override fun onBind(item: String, position: Int) {
        view.text = item
    }
}