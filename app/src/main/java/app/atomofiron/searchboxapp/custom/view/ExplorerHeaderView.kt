package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import app.atomofiron.searchboxapp.*
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerItemActionListener
import app.atomofiron.searchboxapp.screens.explorer.adapter.util.ExplorerItemBinderImpl

class ExplorerHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val colorDrawable = ColorDrawable(0)

    init {
        LayoutInflater.from(context).inflate(R.layout.item_explorer, this)
        clipToPadding = false
        getChildAt(0).background = null
        background = context.rippleDrawable(colorDrawable)
    }

    private val binder = ExplorerItemBinderImpl(this)
    private var composition: ExplorerItemComposition? = null
    private var item: Node? = null
    private var mBottom = 0
    private var insetColor = 0
    private val paint = Paint()

    fun setOnItemActionListener(listener: ExplorerItemActionListener) {
        binder.onItemActionListener = listener
    }

    fun setComposition(composition: ExplorerItemComposition) {
        this.composition = composition
        tryBind()
    }

    fun bind(item: Node?) {
        this.item = item
        tryBind()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (bottom != mBottom) {
            move(mBottom)
        }
    }

    fun move(bottom: Int) {
        mBottom = bottom
        this.top = bottom - measuredHeight
        this.bottom = bottom
    }

    private fun tryBind() {
        val composition = composition ?: return
        val item = item ?: return
        if (!isVisible) isVisible = true
        binder.onBind(item)
        binder.bindComposition(composition)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (paddingTop > 0 && insetColor != 0 && top < 0 && top > -height) {
            paint.style = Paint.Style.FILL
            paint.color = insetColor
            val top = -top.toFloat()
            canvas.drawRect(0f, top, width.toFloat(), top + paddingTop.toFloat(), paint)
        }
    }

    override fun setBackgroundColor(color: Int) {
        insetColor = ColorUtils.setAlphaComponent(color, Byte.MAX_VALUE.toInt())
        colorDrawable.color = color
    }
}