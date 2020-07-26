package app.atomofiron.searchboxapp.screens.viewer.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch

class TextViewerAdapter : GeneralAdapter<TextViewerHolder, TextLine>() {
    var textViewerListener: TextViewerListener? = null
    private var matches: Map<Int, List<TextLineMatch>> = HashMap()

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
            recyclerView?.post {
                recyclerView?.scrollToPosition(cursorLineIndex)
            }
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
        textViewerListener?.onLineVisible(position)
    }

    interface TextViewerListener {
        fun onLineVisible(index: Int)
    }
}