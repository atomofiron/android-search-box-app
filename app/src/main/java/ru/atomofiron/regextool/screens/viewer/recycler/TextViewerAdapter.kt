package ru.atomofiron.regextool.screens.viewer.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import app.atomofiron.common.recycler.GeneralAdapter

class TextViewerAdapter : GeneralAdapter<TextViewerHolder, String>() {
    lateinit var textViewerListener: TextViewerListener

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): TextViewerHolder {
        val textView = TextView(parent.context)
        textView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        return TextViewerHolder(textView)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onBindViewHolder(holder: TextViewerHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        textViewerListener.onLineVisible(position)
    }

    interface TextViewerListener {
        fun onLineVisible(index: Int)
    }
}