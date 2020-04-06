package ru.atomofiron.regextool.view.custom

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText

open class AutoHideKeyboardField @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {
    private var inputMethodManager: InputMethodManager? = null

    init {
        inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (focused) {
            inputMethodManager!!.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            setSelection(length())
        } else {
            inputMethodManager!!.hideSoftInputFromWindow(windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
        }
    }
}