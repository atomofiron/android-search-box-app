package ru.atomofiron.regextool.screens.viewer.recycler

import android.widget.TextView
import app.atomofiron.common.recycler.GeneralHolder
import ru.atomofiron.regextool.model.textviewer.TextLine

class TextViewerHolder(val view: TextView) : GeneralHolder<TextLine>(view) {
    override fun onBind(item: TextLine, position: Int) {
        view.text = item.text
    }
}