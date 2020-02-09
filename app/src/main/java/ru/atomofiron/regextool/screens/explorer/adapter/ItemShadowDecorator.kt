package ru.atomofiron.regextool.screens.explorer.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R

class ItemShadowDecorator(private val shadowType: (position: Int) -> Shadow) : RecyclerView.ItemDecoration() {
    companion object {
        private const val SHADOW_ALPHA = 50
    }
    enum class Shadow {
        NO, TOP, BOTTOM, DOUBLE
    }

    private val comparator = Comparator<Int> { first, second -> first - second }
    private lateinit var topShadow: Drawable
    private lateinit var bottomShadow: Drawable
    private var topShadowSize = 0
    private var bottomShadowSize = 0

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        initShadow(parent.context)

        getChildren(parent).forEach {
            val child = it.value
            val position = parent.getChildLayoutPosition(child)

            if (shadowType(position) == Shadow.NO) {
                return@forEach
            }

            val dynamicOffset = child.bottom * topShadowSize / parent.measuredHeight

            when (shadowType(position)) {
                Shadow.TOP -> drawTop(canvas, child, dynamicOffset)
                Shadow.BOTTOM -> {
                    val rect = Rect()
                    parent.getDecoratedBoundsWithMargins(child, rect)
                    drawBottom(canvas, rect, dynamicOffset / 2)
                }
                Shadow.DOUBLE -> {
                    val rect = Rect()
                    parent.getDecoratedBoundsWithMargins(child, rect)
                    drawTop(canvas, child, dynamicOffset)
                    drawBottom(canvas, rect, dynamicOffset / 2)
                }
                else -> Shadow.NO
            }
        }
    }

    private fun drawTop(canvas: Canvas, child: View, dynamicOffset: Int) {
        topShadow.setBounds(child.left, child.bottom, child.right, child.bottom + topShadowSize + dynamicOffset)
        topShadow.draw(canvas)
    }

    private fun drawBottom(canvas: Canvas, rect: Rect, dynamicOffset: Int) {
        bottomShadow.setBounds(rect.left, rect.bottom - bottomShadowSize + dynamicOffset, rect.right, rect.bottom)
        bottomShadow.draw(canvas)
    }

    private fun initShadow(context: Context) {
        if (::topShadow.isInitialized) {
            return
        }

        topShadow = ContextCompat.getDrawable(context, R.drawable.item_explorer_opened_dir_shadow_top)!!
        bottomShadow = ContextCompat.getDrawable(context, R.drawable.item_explorer_opened_dir_shadow_bottom)!!
        topShadow.alpha = SHADOW_ALPHA
        bottomShadow.alpha = SHADOW_ALPHA
        topShadowSize = context.resources.getDimensionPixelSize(R.dimen.item_dir_shadow_size)
        bottomShadowSize = context.resources.getDimensionPixelSize(R.dimen.item_dir_shadow_size)
    }

    private fun getChildren(parent: RecyclerView): Map<Int, View> {
        val children = mutableMapOf<Int, View>()
        // exclude duplicated items (some items have the same adapter position)
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildLayoutPosition(child)
            children[position] = child
        }
        return children.toSortedMap(comparator)
    }
}