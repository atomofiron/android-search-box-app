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
        val items = adapter.currentList
        val firstIndex = parent.getChildViewHolder(first).bindingAdapterPosition
        val lastIndex = firstIndex + parent.childCount.dec()
        var currentIndex = firstIndex
        var frameRect: RectF? = null
        paint.color = grey
        for (child in parent) {
            var left = parent.paddingLeft + strokeOffset
            var right = parent.width - (parent.paddingRight + strokeOffset)
            val prev = if (currentIndex == firstIndex) null else items.getOrNull(currentIndex.dec())
            val item = items[currentIndex]
            val next = if (currentIndex == lastIndex) null else items.getOrNull(currentIndex.inc())
            val nextParentPath = next?.parentPath ?: ""
            val childBottomEdge = child.bottom + levelSpace / 2
            when {
                item.isOpened && item.isEmpty -> {
                    left += innerPadding / 2
                    right -= innerPadding / 2
                    val top = childBottomEdge + strokeOffset
                    var bottom = childBottomEdge + levelSpace - strokeOffset
                    bottom = max(top, bottom)
                    rect.set(left, top, right, bottom)
                    frameRect = rect
                }
                item.parentPath == currentDir?.path -> {
                    rect.left = left
                    rect.right = right
                    if (item.parentPath != prev?.parentPath) {
                        rect.top = child.top - levelSpace / 2
                    }
                    if (item.parentPath != next?.parentPath) {
                        rect.bottom = childBottomEdge
                    }
                    frameRect = rect
                }
                item.isOpened && item.uniqueId == currentDir?.uniqueId -> Unit
                !item.isOpened && item.isRoot -> Unit
                item.parentPath != nextParentPath -> {
                    left += innerPadding
                    right -= innerPadding
                    canvas.drawLine(left, childBottomEdge, right, childBottomEdge, paint)
                }
            }
            currentIndex++
        }
        frameRect?.let {
            val minTop = ExplorerHeaderDelegate.getHeaderBottom(parent, headerView, adapter, currentDir) + strokeOffset
            frameRect.top = max(frameRect.top, minTop)
            frameRect.bottom = min(frameRect.bottom, parent.height - parent.paddingBottom - strokeOffset)
            val boxHeight = frameRect.height()
            if (boxHeight > 0) {
                val alpha = (255 * (boxHeight / (levelSpace / 2)).coerceIn(0f, 1f)).toInt()
                paint.strokeWidth = strokeWidth
                paint.style = Paint.Style.STROKE
                paint.color = ColorUtils.setAlphaComponent(accent, alpha)
                canvas.drawRoundRect(frameRect, cornerRadius, cornerRadius, paint)
            }
        }
    }
}