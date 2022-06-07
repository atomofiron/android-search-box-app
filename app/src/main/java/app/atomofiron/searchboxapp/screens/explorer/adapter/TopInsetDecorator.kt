package app.atomofiron.searchboxapp.screens.explorer.adapter

import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.getColorByAttr
import app.atomofiron.searchboxapp.utils.Const
import kotlin.math.max

class TopInsetDecorator : RecyclerView.ItemDecoration() {

    private val paint = Paint()
    private var colorsDefined = false
    private var topColorEmpty = 0
    private var topColorGrey = 0
    var useGrey = false

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(canvas, parent, state)

        if (!colorsDefined) {
            colorsDefined = true
            val backgroundColor = parent.context.getColorByAttr(R.attr.colorBackground)
            topColorEmpty = ColorUtils.setAlphaComponent(backgroundColor, Const.ALPHA_50_PERCENT)
            topColorGrey = ContextCompat.getColor(parent.context, R.color.item_explorer_background)
        }

        if (parent.childCount == 0) return
        val child = parent.getChildAt(0)
        if (child.top >= parent.paddingTop) return

        paint.color = if (useGrey) topColorGrey else topColorEmpty
        val top = max(0, child.top).toFloat()
        canvas.drawRect(0f, top, parent.width.toFloat(), parent.paddingTop.toFloat(), paint)
    }
}