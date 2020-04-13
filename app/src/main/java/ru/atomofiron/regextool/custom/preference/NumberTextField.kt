package ru.atomofiron.regextool.custom.preference

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
    companion object {
        private const val ZERO = "0"
    }
    private var onSubmitListener: ((Int) -> Unit)? = null

    init {
        filters = arrayOf<InputFilter>(LengthFilter(9))
        gravity = Gravity.CENTER_HORIZONTAL
        keyListener = DigitsKeyListener.getInstance("0123456789")
        hint = "_____"
    }

    fun setOnSubmitListener(listener: ((Int) -> Unit)?) {
        onSubmitListener = listener
    }

    override fun onSubmit(value: String) {
        onSubmitListener?.invoke(value.toInt())
    }

    override fun afterTextChanged(editable: Editable) {
        val value = editable.toString()

        when {
            value.length > 1 && value.startsWith(ZERO) -> {
                val start = selectionStart
                setText(value.substring(1))
                setSelection(if (start == 0) 0 else start - 1)
            }
            value.isEmpty() -> {
                setText(ZERO)
                setSelection(1)
            }
        }
    }
}