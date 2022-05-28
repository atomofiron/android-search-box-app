package app.atomofiron.searchboxapp.custom.preference

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.getAttr

class DropDownPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleRes: Int = context.getAttr(
        androidx.preference.R.attr.preferenceStyle,
        android.R.attr.preferenceStyle,
    )
) : Preference(context, attrs, defStyleRes), AdapterView.OnItemSelectedListener {
    private lateinit var acSpinner: Spinner
    private val entryValues: Array<String>
    private val entries: Array<String>
    private var initialValue: String? = null
    private val defaultValue: String get() = initialValue ?: entryValues.first()

    init {
        widgetLayoutResource = R.layout.widget_spinner

        val array = context.obtainStyledAttributes(attrs, R.styleable.SpinnerPreference, 0, defStyleRes)
        val entryValuesId = array.getResourceId(R.styleable.SpinnerPreference_entryValues, 0)
        val entriesId = array.getResourceId(R.styleable.SpinnerPreference_entries, 0)
        array.recycle()
        entryValues = context.resources.getStringArray(entryValuesId)
        entries = context.resources.getStringArray(entriesId)
    }

    override fun onGetDefaultValue(array: TypedArray, index: Int): Any? = array.getString(index)

    override fun onSetInitialValue(defaultValue: Any?) {
        initialValue = defaultValue as String?
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        acSpinner = holder.itemView.findViewById(R.id.widget_spinner)
        if (acSpinner.adapter == null) {
            val adapter = ArrayAdapter<CharSequence>(
                context,
                android.R.layout.simple_spinner_item,
                entries
            )
            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
            acSpinner.adapter = adapter
            val persistedValue = getPersistedString(defaultValue)
            acSpinner.setSelection(entryValues.indexOf(persistedValue))
            acSpinner.onItemSelectedListener = this
        }
        holder.itemView.apply {
            if (paddingEnd != 0) {
                setPaddingRelative(paddingStart, paddingTop, 0, paddingBottom)
            }
        }
    }

    override fun onClick() {
        super.onClick()
        acSpinner.performClick()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val persistedValue = getPersistedString(defaultValue)
        if (persistedValue != entryValues[position]) {
            persistString(entryValues[position])
            notifyChanged()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) = Unit

    override fun notifyChanged() {
        super.notifyChanged()
        val persistedValue = getPersistedString(defaultValue)
        callChangeListener(persistedValue)
    }
}