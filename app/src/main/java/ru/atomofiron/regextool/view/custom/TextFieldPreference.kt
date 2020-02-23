package ru.atomofiron.regextool.view.custom

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.RelativeLayout.LayoutParams
import android.widget.RelativeLayout.LayoutParams.MATCH_PARENT
import android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder

class TextFieldPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {
    companion object {
        private const val VISIBLE = 1f
        private const val INVISIBLE = 0f
    }
    private val editText: TextField = TextField(context)
    private lateinit var summary: View
    private var value = ""

    init {
        editText.visibility = View.GONE
        editText.isFocusable = false
        editText.setOnSubmitListener(::onSubmit)
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                summary.alpha = VISIBLE
                editText.visibility = View.GONE
            }
        }
    }

    override fun onGetDefaultValue(array: TypedArray, index: Int): String? = array.getString(index)

    override fun onSetInitialValue(defaultValue: Any?) {
        value = (defaultValue as? String) ?: getPersistedString(value)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        if (editText.parent == null) {
            summary = holder.itemView.findViewById<View>(android.R.id.summary)
            editText.layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                addRule(RelativeLayout.BELOW, android.R.id.title)
            }
            editText.setText(value)
            (summary.parent as ViewGroup).addView(editText, 1)
        }
    }

    public override fun onClick() {
        summary.alpha = INVISIBLE
        editText.visibility = View.VISIBLE
        editText.onClick(editText)
    }

    private fun onSubmit(value: String) {
        callChangeListener(value)
        persistString(value)
        this.value = value
    }
}