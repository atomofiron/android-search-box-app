package app.atomofiron.searchboxapp.screens.explorer.adapter

import android.graphics.*
import android.view.View
import androidx.core.view.iterator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView
import app.atomofiron.searchboxapp.model.explorer.Node
import kotlin.math.max
import kotlin.math.min

class ItemBorderDecorator(
    private val adapter: ExplorerAdapter,
    private val headerView: ExplorerHeaderView,
) : ItemDecoration() {

    private var grey = headerView.context.findColorByAttr(R.attr.colorOutline)
    private var accent = headerView.context.findColorByAttr(R.attr.colorTertiary)
    private var cornerRadius = headerView.resources.getDimension(R.dimen.explorer_border_corner_radius)
    private var borderWidth = headerView.resources.getDimension(R.dimen.explorer_border_width)
    private var levelSpace = headerView.resources.getDimension(R.dimen.explorer_level_space)
    private var innerPadding = headerView.resources.getDimension(R.dimen.content_margin)

    private var currentDir: Node? = null
    private val paint = Paint()
    private val rect = RectF()
    private val framePath = Path()

    init {
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = borderWidth
    }

    fun setCurrentDir(item: Node?) {
        currentDir = item
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

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
        var currentDirMinTop = headerView.height.toFloat()
        paint.color = grey
        for (child in parent) {
            var left = parent.paddingLeft.toFloat()
            var right = parent.width - parent.paddingRight.toFloat()
            val prev = if (currentIndex == firstIndex) null else items.getOrNull(currentIndex.dec())
            val item = items[currentIndex]
            val next = if (currentIndex == lastIndex) null else items.getOrNull(currentIndex.inc())
            val nextParentPath = next?.parentPath ?: ""
            val childBottomOffset = child.bottom + levelSpace / 2
            when {
                item.isOpened && item.isEmpty -> {
                    val top = child.bottom.toFloat()
                    val bottom = child.bottom + levelSpace * 2
                    rect.set(left, top, right, bottom)
                    frameRect = rect
                    currentDirMinTop = -cornerRadius
                }
                item.parentPath == currentDir?.path -> {
                    rect.left = left
                    rect.right = right
                    if (item.parentPath != prev?.parentPath) {
                        rect.top = child.top - levelSpace
                    }
                    if (item.parentPath != next?.parentPath) {
                        rect.bottom = childBottomOffset + borderWidth / 2
                    }
                    frameRect = rect
                }
                item.isOpened && item.uniqueId == currentDir?.uniqueId -> Unit
                !item.isOpened && item.isRoot -> Unit
                item.parentPath != nextParentPath -> {
                    left += innerPadding
                    right -= innerPadding
                    canvas.drawLine(left, childBottomOffset, right, childBottomOffset, paint)
                }
            }
            currentIndex++
        }
        frameRect?.let {
            frameRect.bottom = min(frameRect.bottom, parent.height.toFloat() - parent.paddingBottom)
            val boxHeight = frameRect.height()
            if (boxHeight > 0) {
                paint.color = accent
                framePath.reset()
                val minTop = min(currentDirMinTop, frameRect.bottom - levelSpace / 2)
                framePath.addRoundRect(
                    frameRect.left,
                    max(minTop, frameRect.top),
                    frameRect.right,
                    frameRect.bottom,
                    FloatArray(8) { if (it <= 3) 0f else cornerRadius },
                    Path.Direction.CW,
                )
                framePath.addRoundRect(
                    frameRect.left + borderWidth,
                    max(minTop, frameRect.top),
                    frameRect.right - borderWidth,
                    frameRect.bottom - borderWidth,
                    cornerRadius - borderWidth,
                    cornerRadius - borderWidth,
                    Path.Direction.CCW,
                )
                canvas.drawPath(framePath, paint)
            }
        }
    }
}