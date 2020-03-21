package ru.atomofiron.regextool.screens.explorer.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.screens.explorer.adapter.util.getSortedChildren

class ItemShadowDecorator(private val shadowType: (position: Int) -> Shadow) : RecyclerView.ItemDecoration() {
    companion object {
        private const val SHADOW_ALPHA = 100
    }
    enum class Shadow {
        NO, TOP, TOP_SLIDE, BOTTOM, DOUBLE
    }

    private var initiate = false
    private lateinit var topShadow: Drawable
    private lateinit var bottomShadow: Drawable
    private var shadowSize = 0

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        initShadow(parent.context)

        parent.getSortedChildren().forEach {
            val child = it.value
            val position = parent.getChildLayoutPosition(child)

            val shadowType = shadowType(position)
            if (shadowType == Shadow.NO) {
                return@forEach
            }

            val dynamicOffset = child.bottom * shadowSize / parent.measuredHeight

            when (shadowType) {
                Shadow.TOP -> drawTop(canvas, child, dynamicOffset, pinned = true)
                Shadow.BOTTOM -> {
                    if (child.top <= 0) {
                        drawSlide(canvas, child, pinned = false)
                    }
                    val rect = Rect()
                    parent.getDecoratedBoundsWithMargins(child, rect)
                    drawBottom(canvas, rect)
                }
                Shadow.DOUBLE -> {
                    val rect = Rect()
                    parent.getDecoratedBoundsWithMargins(child, rect)
                    drawTop(canvas, child, dynamicOffset, pinned = false)
                    drawBottom(canvas, rect)
                }
                Shadow.TOP_SLIDE -> {
                    if (child.top <= 0) {
                        drawSlide(canvas, child, pinned = true)
                    }
                }
                Shadow.NO -> Unit
            }
        }
    }

    private fun drawTop(canvas: Canvas, child: View, dynamicOffset: Int, pinned: Boolean) {
        val bottom = when {
            pinned -> Math.max(0, child.bottom)
            else -> child.bottom
        }
        topShadow.setBounds(child.left, bottom, child.right, bottom + shadowSize + dynamicOffset)
        topShadow.draw(canvas)
    }

    private fun drawBottom(canvas: Canvas, rect: Rect) {
        bottomShadow.setBounds(rect.left, rect.bottom - shadowSize, rect.right, rect.bottom)
        bottomShadow.draw(canvas)
    }

    private fun drawSlide(canvas: Canvas, child: View, pinned: Boolean) {
        val top = when {
            pinned -> 0
            else -> child.top
        }
        topShadow.setBounds(child.left, top, child.right, top + shadowSize)
        topShadow.draw(canvas)
    }

    private fun initShadow(context: Context) {
        if (initiate) {
            return
        }
        initiate = true

        topShadow = ContextCompat.getDrawable(context, R.drawable.item_explorer_opened_dir_shadow_top)!!
        bottomShadow = ContextCompat.getDrawable(context, R.drawable.item_explorer_opened_dir_shadow_bottom)!!
        topShadow.alpha = SHADOW_ALPHA
        bottomShadow.alpha = SHADOW_ALPHA
        shadowSize = context.resources.getDimensionPixelSize(R.dimen.item_dir_shadow_size)
    }
}