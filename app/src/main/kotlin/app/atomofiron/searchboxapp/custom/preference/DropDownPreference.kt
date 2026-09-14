package app.atomofiron.searchboxapp.custom.preference

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.preference.DropDownPreference
import androidx.preference.PreferenceViewHolder
import app.atomofiron.common.util.extension.tryAs
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.utils.drawable

@SuppressLint("PrivateResource")
class DropDownPreference(
    context: Context,
    attrs: AttributeSet? = null,
) : DropDownPreference(context, attrs) {

    private var interceptClicks = false

    init {
        layoutResource = androidx.preference.R.layout.preference_material
        widgetLayoutResource = R.layout.widget_spinner
        super.setSummary(null)
    }

    override fun createAdapter(): ArrayAdapter<*> = ArrayAdapter<Any?>(context, R.layout.item_drop_down)

    override fun setSummary(summary: CharSequence?) = super.setSummary(null)

    override fun onClick() {
        if (!interceptClicks) super.onClick()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView
            .findViewById<AppCompatSpinner>(androidx.preference.R.id.spinner)
            .background
            .tryAs<LayerDrawable>()
            ?.setDrawable(1, context.drawable(R.drawable.ic_arrow_drop_down))
    }

    fun interceptClicks() {
        interceptClicks = true
    }
}