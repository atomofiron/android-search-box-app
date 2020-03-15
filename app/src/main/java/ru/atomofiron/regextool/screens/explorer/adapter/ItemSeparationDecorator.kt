package ru.atomofiron.regextool.screens.explorer.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R

class ItemSeparationDecorator(private val separationType: (position: Int) -> Separation) : RecyclerView.ItemDecoration() {
    enum class Separation {
        NO, TOP, BOTTOM
    }

    private val comparator = Comparator<Int> { first, second -> first - second }
    private lateinit var upSeparation: Drawable
    private lateinit var downSeparation: Drawable
    private var separationSize = 0

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        initShadow(parent.context)

        getChildren(parent).forEach {
            val child = it.value
            val position = parent.getChildLayoutPosition(child)

            if (separationType(position) == Separation.NO) {
                return@forEach
            }

            when (separationType(position)) {
                Separation.TOP -> drawUp(canvas, child)
                Separation.BOTTOM -> drawDown(canvas, child)
                Separation.NO -> Unit
            }
        }
    }

    private fun drawUp(canvas: Canvas, child: View) {
        val left = child.right / 3
        val right = child.right - left
        val offset = child.measuredHeight / 4 - separationSize / 2
        upSeparation.setBounds(left, child.bottom + offset, right, child.bottom + offset + separationSize)
        upSeparation.draw(canvas)
    }

    private fun drawDown(canvas: Canvas, child: View) {
        val left = child.right / 3
        val right = child.right - left
        val offset = child.measuredHeight / 4 + separationSize / 2
        downSeparation.setBounds(left, child.bottom + offset - separationSize, right, child.bottom + offset)
        downSeparation.draw(canvas)
    }

    private fun initShadow(context: Context) {
        if (::upSeparation.isInitialized) {
            return
        }

        upSeparation = ContextCompat.getDrawable(context, R.drawable.tree_level_separation)!!
        downSeparation = ContextCompat.getDrawable(context, R.drawable.tree_level_separation)!!
        separationSize = context.resources.getDimensionPixelSize(R.dimen.item_dir_separation_size)
        var color = ContextCompat.getColor(context, R.color.colorAccent)
        upSeparation.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        color = ContextCompat.getColor(context, R.color.grey_middle_lite)
        downSeparation.setColorFilter(color, PorterDuff.Mode.SRC_IN)
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