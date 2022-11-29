package app.atomofiron.searchboxapp.screens.explorer.adapter

import android.graphics.*
import android.graphics.Path.Direction
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

    private val items get() = adapter.currentList
    private val grey = headerView.context.findColorByAttr(R.attr.colorOutline)
    private val accent = headerView.context.findColorByAttr(R.attr.colorTertiary)
    private val innerPadding = headerView.resources.getDimension(R.dimen.content_margin)
    private val cornerRadius = headerView.resources.getDimension(R.dimen.explorer_border_corner_radius)
    private val borderWidth = headerView.resources.getDimension(R.dimen.explorer_border_width)
    // под открытой пустой директорией
    private val emptySpace = cornerRadius * 2
    // под открытой не пустой директорией
    private val openedSpace = cornerRadius
    // под последним айтемом глубочайшей директории
    private val openedEndSpace = cornerRadius * 2
    // расстояние между низом последнего айтема глубочайшей директории и нижним краем рамки,
    // а так же минимальное расстояние между низом открытой директории и нижним краем рамки
    private val frameBottomOffset = openedEndSpace / 2 + borderWidth / 2

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
        val item = items[holder.bindingAdapterPosition]
        val next = items.getOrNull(holder.bindingAdapterPosition.inc())
        outRect.bottom = when {
            item.isOpened && item.isEmpty -> emptySpace
            item.isOpened -> openedSpace
            item.parentPath != next?.parentPath -> openedEndSpace
            else -> return
        }.toInt()
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val first = parent.getChildAt(0)
        first ?: return
        val firstIndex = parent.getChildViewHolder(first).bindingAdapterPosition
        val lastIndex = firstIndex + parent.childCount.dec()
        var currentIndex = firstIndex
        var frameRect: RectF? = null
        val left = parent.paddingLeft.toFloat()
        val right = parent.width - parent.paddingRight.toFloat()
        val headerBottom = headerView.height.toFloat()
        val parentBottom = parent.height.toFloat() - parent.paddingBottom
        paint.color = grey
        for (child in parent) {
            val prev = if (currentIndex == firstIndex) null else items.getOrNull(currentIndex.dec())
            val item = items[currentIndex]
            val next = if (currentIndex == lastIndex) null else items.getOrNull(currentIndex.inc())
            when {
                // под открытой пустой папкой всё просто
                item.isOpened && item.isEmpty -> {
                    frameRect = rect
                    val top = child.bottom.toFloat()
                    val bottom = child.bottom + emptySpace
                    rect.set(left, top, right, bottom)
                }
                // под глубочайшей открытой директорией задаём с рассчётом на то,
                // что дочерние айтемы может быть не видно
                item.isOpened && item.path == currentDir?.path -> {
                    frameRect = rect
                    rect.left = left
                    rect.right = right
                    rect.top = child.bottom.toFloat()
                    rect.bottom = child.bottom + frameBottomOffset
                }
                item.parentPath == currentDir?.path -> {
                    frameRect = rect
                    rect.left = left
                    rect.right = right
                    // верхняя граница рамки или у низа хедера текущей директории,
                    // или у низа айтема текущей директории
                    if (item.parentPath != prev?.parentPath) {
                        rect.top = child.top - openedSpace
                        rect.top = max(rect.top, headerBottom)
                    }
                    // top: хедер уезжает вместе с низом последнего айтема текущей директории
                    // bottom: указываем на нижнюю границу рамки,
                    // которая не должна быть ниже области видимости,
                    // но только если айтем текущей директории не оказывается слишком низко,
                    // чтобы игнорировать область видимости
                    if (item.parentPath != next?.parentPath) {
                        rect.top = min(rect.top, child.bottom.toFloat())
                        rect.bottom = child.bottom + frameBottomOffset
                        rect.bottom = min(rect.bottom, parentBottom)
                        rect.bottom = max(rect.bottom, rect.top + frameBottomOffset)
                    }
                }
                !item.isOpened && item.isRoot -> Unit
                item.parentPath != next?.parentPath -> {
                    val bottom = child.bottom + openedSpace / 2
                    canvas.drawLine(left + innerPadding, bottom, right - innerPadding, bottom, paint)
                }
            }
            currentIndex++
        }
        frameRect?.drawFrame(canvas)
    }

    private fun RectF.drawFrame(canvas: Canvas) {
        val stroke = borderWidth
        val innerRadius = cornerRadius - stroke
        val radii = FloatArray(8) { if (it <= 3) 0f else cornerRadius }
        paint.color = accent
        framePath.reset()
        framePath.addRoundRect(left, top, right, bottom, radii, Direction.CW)
        framePath.addRoundRect(left + stroke, top, right - stroke, bottom - stroke, innerRadius, innerRadius, Direction.CCW)
        canvas.drawPath(framePath, paint)
    }
}