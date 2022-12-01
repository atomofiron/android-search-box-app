package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.*
import app.atomofiron.searchboxapp.databinding.ItemExplorerBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerItemActionListener
import app.atomofiron.searchboxapp.screens.explorer.adapter.util.ExplorerItemBinderImpl

class ExplorerHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    companion object {
        fun ItemExplorerBinding.makeSeparator() = makeOpened()

        fun ItemExplorerBinding.makeOpened() {
            val background = root.context.findColorByAttr(R.attr.colorSecondary)
            val content = root.context.findColorByAttr(R.attr.colorSurface)
            val buttonIcon = root.context.findColorByAttr(R.attr.colorOnSurface)
            makeOpposite(background, content, buttonIcon, topRadius = true, bottomRadius = true)
        }

        fun ItemExplorerBinding.makeOpenedCurrent() {
            val background = root.context.findColorByAttr(R.attr.colorTertiary)
            val content = root.context.findColorByAttr(R.attr.colorSurface)
            val buttonIcon = root.context.findColorByAttr(R.attr.colorOnSurface)
            makeOpposite(background, content, buttonIcon, topRadius = true)
        }

        fun ItemExplorerBinding.makeOpposite(
            background: Int,
            content: Int,
            buttonIcon: Int,
            topRadius: Boolean = false,
            bottomRadius: Boolean = false,
        ) {
            val cornerRadius = root.resources.getDimension(R.dimen.explorer_border_corner_radius)
            val drawable = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(background, background))
            drawable.cornerRadii = FloatArray(8) {
                when {
                    it < 5 && topRadius -> cornerRadius
                    it > 4 && bottomRadius -> cornerRadius
                    else -> 0f
                }
            }
            root.background = RippleDrawable(ColorStateList.valueOf(content), drawable, null)
            val filter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(content, BlendModeCompat.SRC_IN)
            itemExplorerIvIcon.colorFilter = filter
            itemExplorerCb.buttonTintList = ColorStateList.valueOf(content)
            itemExplorerCb.buttonIconTintList = ColorStateList.valueOf(buttonIcon)
            itemExplorerTvTitle.setTextColor(content)
            itemExplorerTvDate.setTextColor(content)
            itemExplorerTvSize.setTextColor(content)
            itemExplorerTvDescription.setTextColor(content)
            itemExplorerErrorTv.setTextColor(content)
            itemExplorerPs.setColor(content)
        }
    }

    private val binder = LayoutInflater.from(context).inflate(R.layout.item_explorer, this).run {
        val binding = ItemExplorerBinding.bind(getChildAt(0))
        binding.makeOpenedCurrent()
        ExplorerItemBinderImpl(binding.root)
    }

    private var composition: ExplorerItemComposition? = null
    private var item: Node? = null
    private var mTop = 0
    private var insetColor = 0
    private val paint = Paint()

    init {
        val colorSurface = context.findColorByAttr(R.attr.colorSurface)
        val colorSurfaceVariant = context.findColorByAttr(R.attr.colorSurfaceVariant)
        var backgroundColor = ColorUtils.setAlphaComponent(colorSurfaceVariant, Byte.MAX_VALUE.toInt())
        backgroundColor = ColorUtils.compositeColors(backgroundColor, colorSurface)
        setBackgroundColor(backgroundColor)
        insetColor = ColorUtils.setAlphaComponent(backgroundColor, Byte.MAX_VALUE.toInt())
    }

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
        if (top != mTop) {
            val height = bottom - top
            move(mTop + height)
        }
    }

    fun move(bottom: Int) {
        mTop = bottom - height
        this.top = mTop
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
}