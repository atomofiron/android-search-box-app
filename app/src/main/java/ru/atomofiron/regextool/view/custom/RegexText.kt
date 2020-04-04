package ru.atomofiron.regextool.view.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class RegexText @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs), TextWatcher {
    companion object {
        private const val UNKNOWN = -1
        private const val ZERO_CHAR = 0.toChar()
    }
    private var locked = false
    private var deleted = ZERO_CHAR
    private var start = 0
    private var count = 0

    private val openBrackets = charArrayOf('[', '{', '(')
    private val closeBrackets = charArrayOf(']', '}', ')')

    init {
        addTextChangedListener(this)
    }

    override fun beforeTextChanged(sequence: CharSequence, start: Int, count: Int, after: Int) {
        deleted = if (count == 1) sequence[start] else ZERO_CHAR
    }

    override fun onTextChanged(sequence: CharSequence, start: Int, before: Int, count: Int) {
        this.start = start
        this.count = count
    }

    override fun afterTextChanged(editable: Editable) {
        if (locked) return
        locked = true

        if (editable.length > start && deleted in openBrackets && editable[start] in closeBrackets) {
            editable.delete(start, start.inc())
        } else if (count == 1) {
            val position = start.inc()
            val char = editable[start]
            val index = openBrackets.indexOf(char)
            if (index != UNKNOWN) {
                editable.insert(position, closeBrackets[index].toString())
                setSelection(start)
            }
        }
        locked = false
    }
}