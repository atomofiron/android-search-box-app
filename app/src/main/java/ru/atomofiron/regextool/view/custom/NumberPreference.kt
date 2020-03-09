package ru.atomofiron.regextool.view.custom

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import ru.atomofiron.regextool.R

class NumberPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {
    private var editText: NumberTextField? = null
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
        if (editText == null) {
            val editText = holder.findViewById(R.id.number) as NumberTextField
            editText.isFocusable = false
            editText.setOnSubmitListener(::onSubmit)
            editText.setText(value.toString())
            this.editText = editText
        }
    }

    override fun onDetached() {
        super.onDetached()
        editText = null
    }

    public override fun onClick() {
        editText!!.performClick()
    }

    private fun onSubmit(value: Int) {
        if (callChangeListener(value)) {
            persistInt(value)
            this.value = value
        }
    }
}