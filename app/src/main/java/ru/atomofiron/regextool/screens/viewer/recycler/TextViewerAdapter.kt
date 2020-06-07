package ru.atomofiron.regextool.screens.viewer.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralAdapter
import ru.atomofiron.regextool.model.textviewer.TextLine
import ru.atomofiron.regextool.model.textviewer.TextLineMatch

class TextViewerAdapter : GeneralAdapter<TextViewerHolder, TextLine>() {
    lateinit var textViewerListener: TextViewerListener
    private lateinit var matches: Map<Int, List<TextLineMatch>>

    private var cursor: Long? = null
    private val cursorLineIndex: Int? get() = cursor?.shr(32)?.toInt()
    private val cursorMatchIndex: Int? get() = cursor?.toInt()

    private var recyclerView: RecyclerView? = null

    init {
        setHasStableIds(true)
    }

    fun setMatches(items: Map<Int, List<TextLineMatch>>) {
        matches = items
        notifyDataSetChanged()
    }

    fun setCursor(lineNumIndex: Long?) {
        val cursorLineIndexWas = cursorLineIndex
        cursor = lineNumIndex

        if (cursorLineIndexWas != null) {
            notifyItemChanged(cursorLineIndexWas)
        }
        val cursorLineIndex = cursorLineIndex
        if (cursorLineIndex != null) {
            notifyItemChanged(cursorLineIndex)
            recyclerView?.scrollToPosition(cursorLineIndex)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): TextViewerHolder {
        val textView = TextView(parent.context)
        textView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        return TextViewerHolder(textView)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onBindViewHolder(holder: TextViewerHolder, position: Int) {
        val indexFocus = when (position) {
            cursorLineIndex -> cursorMatchIndex
            else -> null
        }
        holder.onBind(items[position], matches[position], indexFocus)
        textViewerListener.onLineVisible(position)
    }

    interface TextViewerListener {
        fun onLineVisible(index: Int)
    }
}