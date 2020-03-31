package ru.atomofiron.regextool.view.custom

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class RegexText @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs), TextWatcher {
    companion object {
        private const val UNKNOWN = -1
    }

    private var removePosition = UNKNOWN
    private var selectionPosition = UNKNOWN

    init {
        filters = arrayOf<InputFilter>(BracketsFilter())
        addTextChangedListener(this)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        if (selectionPosition != UNKNOWN) {
            setSelection(selectionPosition)
            selectionPosition = UNKNOWN
        }
    }

    override fun afterTextChanged(editable: Editable) {
        if (removePosition != UNKNOWN) {
            val position = removePosition
            removePosition = UNKNOWN
            editable.replace(position, position.inc(), "")
        }
    }

    private inner class BracketsFilter : InputFilter {
        private val brackets = arrayOf('[', '{', '(')
        private val bracketsClose = arrayOf(']', '}', ')')

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {

            if (source.length == 1) {
                val index = brackets.indexOf(source[0])
                if (index != UNKNOWN) {
                    selectionPosition = dstart.inc()
                    return "${source[0]}${bracketsClose[index]}"
                }
            } else if (source.isEmpty() && dstart.inc() == dend) {
                val index = brackets.indexOf(dest[dstart])
                if (index != -1) {
                    removePosition = dstart
                }
            }
            return null
        }
    }
}