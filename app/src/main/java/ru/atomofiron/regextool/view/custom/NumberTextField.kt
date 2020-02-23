package ru.atomofiron.regextool.view.custom

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.view.Gravity

class NumberTextField @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : TextField(context, attrs) {
    private var onInputListener: ((Int) -> Unit)? = null

    init {
        filters = arrayOf<InputFilter>(LengthFilter(9))
        gravity = Gravity.CENTER_HORIZONTAL
        keyListener = DigitsKeyListener.getInstance("0123456789")
        hint = "_____"
    }

    fun setOnInputListener(listener: ((Int) -> Unit)?) {
        onInputListener = listener
    }

    override fun afterTextChanged(editable: Editable) {
        val value = editable.toString()

        when {
            value.length > 1 && value.startsWith("0") -> {
                val start = selectionStart
                setText(value.substring(1))
                setSelection(if (start == 0) 0 else start - 1)
            }
            value.isEmpty() -> {
                setText("0")
                setSelection(1)
            }
            else -> onInputListener?.invoke(value.toInt())
        }
    }
}