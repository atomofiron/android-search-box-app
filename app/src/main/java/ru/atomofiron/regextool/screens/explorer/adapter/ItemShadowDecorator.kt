package ru.atomofiron.regextool.screens.explorer.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R

class ItemShadowDecorator(private val shadowType: (Int) -> Shadow) : RecyclerView.ItemDecoration() {
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

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        initShadow(parent.context)

        getChildren(parent).forEach {
            val child = it.value
            val position = parent.getChildLayoutPosition(child)
            when (shadowType(position)) {
                Shadow.NO -> Unit
                Shadow.TOP -> drawTop(child, canvas)
                Shadow.BOTTOM -> drawBottom(child, canvas, child.measuredHeight / 2)
                Shadow.DOUBLE -> {
                    drawTop(child, canvas)
                    drawBottom(child, canvas, child.measuredHeight)
                }
            }
        }
    }

    private fun drawTop(child: View, canvas: Canvas) {
        topShadow.setBounds(0, child.bottom, child.measuredWidth, child.bottom + topShadowSize)
        topShadow.draw(canvas)
    }

    private fun drawBottom(child: View, canvas: Canvas, spaceSize: Int) {
        bottomShadow.setBounds(0, child.bottom + spaceSize - bottomShadowSize, child.measuredWidth, child.bottom + spaceSize)
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
        topShadowSize = context.resources.getDimensionPixelSize(R.dimen.item_dir_shadow_size) * 4 / 3
        bottomShadowSize = context.resources.getDimensionPixelSize(R.dimen.item_dir_shadow_size) * 2 / 3
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