package ru.atomofiron.regextool.view.custom

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText

open class TextField @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs), View.OnClickListener, TextWatcher {
    private var inputMethodManager: InputMethodManager? = null
    private var onInputListener: ((String) -> Unit)? = null

    init {
        inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        addTextChangedListener(this)
        imeOptions = EditorInfo.IME_ACTION_DONE
        inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        setOnClickListener(this)
        setSingleLine(true)
        isLongClickable = false
        setHintTextColor(0)
    }

    open fun setOnInputListener(listener: ((String) -> Unit)?) {
        onInputListener = listener
    }

    override fun isSuggestionsEnabled(): Boolean = false

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
    override fun afterTextChanged(editable: Editable) {
        onInputListener?.invoke(text.toString())
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && hasFocus()) {
            isFocusable = false
            return true // перехватываем событие, чтобы оно не обработалось при уже закрытой клавиатуре
        }
        return false
    }

    override fun onEditorAction(actionCode: Int) {
        super.onEditorAction(actionCode)
        if (actionCode == EditorInfo.IME_ACTION_DONE) {
            isFocusable = false
        }
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (focused) {
            inputMethodManager!!.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            setSelection(length())
        } else {
            inputMethodManager!!.hideSoftInputFromWindow(windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
            isFocusable = false
        }
    }

    override fun onClick(view: View) {
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
    }
}