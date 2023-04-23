package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.RecyclerView

class WideRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val parent = parent as View
        if (paddingStart != parent.paddingStart || paddingEnd != parent.paddingEnd) {
            updateLayoutParams<MarginLayoutParams> {
                marginStart = -parent.paddingStart
                marginEnd = -parent.paddingEnd
            }
            updatePaddingRelative(start = parent.paddingStart, end = parent.paddingEnd)
        }
        val customWidthSpec = MeasureSpec.makeMeasureSpec(parent.width, MeasureSpec.getMode(widthSpec))
        super.onMeasure(customWidthSpec, heightSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, top: Int, r: Int, bottom: Int) {
        val parent = parent as View
        super.onLayout(changed, 0, top, parent.width, bottom)
    }
}