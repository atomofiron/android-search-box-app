package ru.atomofiron.regextool.custom.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import ru.atomofiron.regextool.R

open class AutoHideKeyboardField : AppCompatEditText {
    private var inputMethodManager: InputMethodManager? = null

    var hideKeyboardOnFocusLost: Boolean = true

    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        val styled = context.obtainStyledAttributes(attrs, R.styleable.AutoHideKeyboardField, defStyleAttr, 0)
        hideKeyboardOnFocusLost = styled.getBoolean(R.styleable.AutoHideKeyboardField_hideKeyboardOnFocusLost, hideKeyboardOnFocusLost)
        styled.recycle()
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        when {
            !hideKeyboardOnFocusLost -> Unit
            focused -> {
                inputMethodManager!!.showSoftInput(this, 0)
                setSelection(length())
            }
            else -> inputMethodManager!!.hideSoftInputFromWindow(windowToken, 0)
        }
    }

    override fun onDetachedFromWindow() {
        inputMethodManager!!.hideSoftInputFromWindow(windowToken, 0)
        super.onDetachedFromWindow()
    }
}