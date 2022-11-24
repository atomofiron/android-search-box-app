package app.atomofiron.searchboxapp.screens.explorer.adapter

import android.graphics.*
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.core.view.iterator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerHeaderDelegate
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class ItemBorderDecorator(
    private val adapter: ExplorerAdapter,
    private val headerView: ExplorerHeaderView,
) : ItemDecoration() {

    private var currentDir: Node? = null
    private val paint = Paint()
    private var grey = 0
    private var accent = 0
    private var background = 0
    private val rect = RectF()
    private var cornerRadius = 0f
    private var strokeWidth = 0f
    private var strokeOffset = 0f
    private var levelSpace = 0f
    private var innerPadding = 0f

    init {
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 20f
    }

    fun setCurrentDir(item: Node?) {
        currentDir = item
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        grey = parent.context.findColorByAttr(R.attr.colorOutline)
        accent = parent.context.findColorByAttr(R.attr.colorTertiary)
        background = parent.context.findColorByAttr(R.attr.colorBackground)
        cornerRadius = view.resources.getDimension(R.dimen.explorer_border_corner_radius)
        strokeWidth = view.resources.getDimension(R.dimen.explorer_border_width)
        strokeOffset = floor(strokeWidth / 2f)
        levelSpace = view.resources.getDimension(R.dimen.explorer_level_space)
        innerPadding = view.resources.getDimension(R.dimen.content_margin)

        val holder = parent.getChildViewHolder(view)
        holder ?: return
        val item = adapter.currentList[holder.bindingAdapterPosition]
        val next = adapter.currentList.getOrNull(holder.bindingAdapterPosition.inc())
        when {
            item.isOpened && item.isEmpty -> outRect.bottom = levelSpace.toInt() * 2
            item.parentPath != next?.parentPath -> outRect.bottom = levelSpace.toInt()
        }
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val first = parent.getChildAt(0)
        first ?: return
        var index = parent.getChildViewHolder(first).bindingAdapterPosition
        val lastIndex = index + parent.childCount.dec()
        var boxRect: RectF? = null
        for (child in parent) {
            val left = child.paddingLeft + strokeOffset
            val right = parent.width - (child.paddingRight + strokeOffset)
            val prev = adapter.currentList.getOrNull(index.dec())
            val item = adapter.currentList[index]
            val next = adapter.currentList.getOrNull(index.inc())
            val nextParentPath = next?.parentPath ?: ""
            if (item.isOpened && item.isEmpty) {
                val top = child.bottom + levelSpace / 2 + strokeOffset
                var bottom = child.bottom + levelSpace * 1.5f - strokeOffset
                bottom = max(top, bottom)
                rect.set(left + innerPadding / 2, top, right - innerPadding / 2, bottom)
                boxRect = rect
            } else if (item.parentPath == currentDir?.path) {
                rect.left = left
                rect.right = right
                if (item.parentPath != prev?.parentPath) {
                    rect.top = child.top - levelSpace / 2
                }
                if (item.parentPath != next?.parentPath || index == lastIndex) {
                    rect.bottom = child.bottom + levelSpace / 2
                }
                boxRect = rect
            } else if (item.isOpened && item.uniqueId == currentDir?.uniqueId) {
            } else if (!item.isOpened && item.isRoot) {
            } else if (item.parentPath != nextParentPath) {
                paint.color = grey
                canvas.drawLine(left + innerPadding, child.bottom + levelSpace / 2, right - innerPadding, child.bottom + levelSpace / 2, paint)
            }
            index++
        }
        boxRect?.let {
            val minTop = ExplorerHeaderDelegate.getHeaderBottom(parent, headerView, adapter, currentDir) + strokeOffset
            boxRect.top = max(boxRect.top, minTop)
            boxRect.bottom = min(boxRect.bottom, parent.height - parent.paddingBottom - strokeOffset)
            val boxHeight = boxRect.height()
            if (boxHeight > 0) {
                val alpha = (255 * min(1f, max(0f, boxHeight / (levelSpace / 2)))).toInt()
                paint.color = background
                paint.strokeWidth = strokeWidth * 2
                canvas.drawRoundRect(boxRect.left - strokeWidth, boxRect.top - strokeWidth, boxRect.right + strokeWidth, boxRect.bottom + strokeWidth, cornerRadius, cornerRadius, paint)

                paint.strokeWidth = strokeWidth
                paint.color = ColorUtils.setAlphaComponent(accent, alpha)
                canvas.drawRoundRect(boxRect, cornerRadius, cornerRadius, paint)
            }
        }
    }
}