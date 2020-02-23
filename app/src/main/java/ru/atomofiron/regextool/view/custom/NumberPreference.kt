package ru.atomofiron.regextool.view.custom

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.view.custom.NumberText.OnInputListener

class NumberPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs), OnInputListener {
    private lateinit var editText: NumberText
    private var value = 0

    init {
        widgetLayoutResource = R.layout.edittext_number
    }

    override fun onGetDefaultValue(array: TypedArray, index: Int): Int = array.getInt(index, 0)

    override fun onSetInitialValue(defaultValue: Any?) {
        value = (defaultValue as? Int) ?: getPersistedInt(value)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        if (!::editText.isInitialized) {
            editText = holder.findViewById(R.id.number) as NumberText
            editText.isFocusable = false
            editText.setOnInputListener(this)
            editText.setText(value.toString())
        }
    }

    public override fun onClick() = editText.onClick(editText)

    override fun onInput(value: Int) {
        callChangeListener(value)
        persistInt(value)
        this.value = value
    }
}