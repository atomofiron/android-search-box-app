package ru.atomofiron.regextool.view.custom

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText

open class TextField @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs), TextWatcher {
    private var inputMethodManager: InputMethodManager? = null
    private var onSubmitListener: ((String) -> Unit)? = null

    private var submittedValue: CharSequence = ""

    init {
        inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        addTextChangedListener(this)
        imeOptions = EditorInfo.IME_ACTION_DONE
        inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        setSingleLine(true)
        isLongClickable = false
        setHintTextColor(0)
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)

        submittedValue = text ?: ""
    }

    open fun setOnSubmitListener(listener: ((String) -> Unit)?) {
        onSubmitListener = listener
    }

    open fun onSubmit(value: String) {
        onSubmitListener?.invoke(value)
    }

    override fun isSuggestionsEnabled(): Boolean = false

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit

    override fun afterTextChanged(editable: Editable) = Unit

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && hasFocus()) {
            isFocusable = false
            // перехватываем событие, чтобы оно не обработалось при уже закрытой клавиатуре
            return true
        }
        return false
    }

    override fun onEditorAction(actionCode: Int) {
        super.onEditorAction(actionCode)
        if (actionCode == EditorInfo.IME_ACTION_DONE) {
            submittedValue = text.toString()
            onSubmit(text.toString())
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
            setText(submittedValue, BufferType.NORMAL)
        }
    }

    override fun performClick(): Boolean {
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
        return super.performClick()
    }
}