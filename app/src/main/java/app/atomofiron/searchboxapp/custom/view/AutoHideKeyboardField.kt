package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import app.atomofiron.searchboxapp.R

open class AutoHideKeyboardField : AppCompatEditText {
    private var inputMethodManager: InputMethodManager? = null

    protected var hideKeyboardOnFocusLost: Boolean = true
    protected var hideKeyboardOnDetached: Boolean = true

    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val styled = context.obtainStyledAttributes(attrs, R.styleable.AutoHideKeyboardField, defStyleAttr, 0)
        hideKeyboardOnFocusLost = styled.getBoolean(R.styleable.AutoHideKeyboardField_hideKeyboardOnFocusLost, hideKeyboardOnFocusLost)
        styled.recycle()
    }

    init {
        inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if ((keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK) && hasFocus()) {
            clearFocus()
            inputMethodManager!!.hideSoftInputFromWindow(windowToken, 0)
            // перехватываем событие, чтобы оно не обработалось при уже закрытой клавиатуре
            return true
        }
        return false
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        val keyboard = resources.configuration.keyboard
        when {
            !hideKeyboardOnFocusLost -> Unit
            keyboard == Configuration.KEYBOARD_QWERTY -> Unit
            !focused -> inputMethodManager!!.hideSoftInputFromWindow(windowToken, 0)
            else -> {
                inputMethodManager!!.showSoftInput(this, 0)
                setSelection(length())
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (hideKeyboardOnDetached) {
            inputMethodManager!!.hideSoftInputFromWindow(windowToken, 0)
        }
    }
}