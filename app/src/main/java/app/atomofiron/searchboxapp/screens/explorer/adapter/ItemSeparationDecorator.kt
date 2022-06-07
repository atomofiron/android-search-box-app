package app.atomofiron.searchboxapp.screens.explorer.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.explorer.adapter.util.getSortedChildren

class ItemSeparationDecorator(private val separationType: (position: Int) -> Separation) : RecyclerView.ItemDecoration() {
    enum class Separation {
        NO, TOP, BOTTOM
    }

    private lateinit var upSeparation: Drawable
    private lateinit var downSeparation: Drawable
    private var separationSize = 0

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        initShadow(parent.context, forced = false)

        parent.getSortedChildren().forEach {
            val child = it.value
            val position = parent.getChildLayoutPosition(child)

            when (separationType(position)) {
                Separation.NO -> Unit
                Separation.TOP -> drawUp(canvas, child)
                Separation.BOTTOM -> drawDown(canvas, child)
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

    fun onAttachedToRecyclerView(context: Context) = initShadow(context, forced = true)

    private fun initShadow(context: Context, forced: Boolean) {
        if (!forced && ::upSeparation.isInitialized) {
            return
        }

        upSeparation = ContextCompat.getDrawable(context, R.drawable.tree_level_separation)!!
        downSeparation = ContextCompat.getDrawable(context, R.drawable.tree_level_separation)!!
        separationSize = context.resources.getDimensionPixelSize(R.dimen.item_dir_separation_size)
        var color = context.findColorByAttr(R.attr.colorAccent)
        upSeparation.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        color = ContextCompat.getColor(context, R.color.grey_middle_lite)
        downSeparation.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}