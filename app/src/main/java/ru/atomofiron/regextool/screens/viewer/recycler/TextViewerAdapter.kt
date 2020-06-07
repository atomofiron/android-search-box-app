package ru.atomofiron.regextool.screens.viewer.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import app.atomofiron.common.recycler.GeneralAdapter
import ru.atomofiron.regextool.model.textviewer.TextLine

class TextViewerAdapter : GeneralAdapter<TextViewerHolder, TextLine>() {
    lateinit var textViewerListener: TextViewerListener
    private lateinit var matches: Map<Int, List<TextLine.Match>?>

    private var cursor: Long? = null
    private val lineIndex: Int? = cursor?.shr(32)?.toInt()
    private val matchIndex: Int? get() = cursor?.toInt()

    init {
        setHasStableIds(true)
    }

    fun setMatches(items: Map<Int, List<TextLine.Match>?>) {
        matches = items
        notifyDataSetChanged()
    }

    fun setCursor(lineNumIndex: Long?) {
        val lineIndexWas = lineIndex
        cursor = lineNumIndex

        if (lineIndexWas != null) {
            notifyItemChanged(lineIndexWas)
        }
        val lineIndex = lineIndex
        if (lineIndex != null) {
            notifyItemChanged(lineIndex)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): TextViewerHolder {
        val textView = TextView(parent.context)
        textView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        return TextViewerHolder(textView)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onBindViewHolder(holder: TextViewerHolder, position: Int) {
        holder.onBind(items[position], matches[position])
        textViewerListener.onLineVisible(position)
    }

    interface TextViewerListener {
        fun onLineVisible(index: Int)
    }
}