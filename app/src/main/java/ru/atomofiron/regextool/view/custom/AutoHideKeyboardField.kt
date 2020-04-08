package ru.atomofiron.regextool.view.custom

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import ru.atomofiron.regextool.R

open class AutoHideKeyboardField @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {
    private var inputMethodManager: InputMethodManager? = null

    protected open val hideKeyboardOnFocusLost: Boolean

    init {
        inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        val styled = context.obtainStyledAttributes(attrs, R.styleable.AutoHideKeyboardField, defStyleAttr, 0)
        hideKeyboardOnFocusLost = styled.getBoolean(R.styleable.AutoHideKeyboardField_hideKeyboardOnFocusLost, true)
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