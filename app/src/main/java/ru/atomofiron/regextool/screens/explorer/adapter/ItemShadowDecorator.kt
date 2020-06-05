package ru.atomofiron.regextool.screens.explorer.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.findColorByAttr
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.ExplorerHeaderView
import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.screens.explorer.adapter.util.getSortedChildren
import kotlin.math.max

class ItemShadowDecorator(private val items: List<XFile>) : RecyclerView.ItemDecoration() {
    companion object {
        private const val UNDEFINED = -1
        private const val SHADOW_ALPHA = 100
    }

    private var initiate = false
    private lateinit var topShadow: Drawable
    private lateinit var bottomShadow: Drawable
    private var shadowSize = 0

    lateinit var headerView: ExplorerHeaderView
    private val topLimit: Int get() = headerView.measuredHeight

    private var headerPosition: Int = UNDEFINED
    private lateinit var background: ShapeDrawable
    private var headerItem: XFile? = null

    private var backgroundClear = 0
    private var backgroundGrey = 0

    fun onHeaderChanged(item: XFile?) {
        headerPosition = UNDEFINED
        headerItem = item
        item ?: return

        headerPosition = items.indexOf(item)
        val wasGone = headerView.visibility == View.GONE
        headerView.onBind()
        if (wasGone) {
            // чтобы не мелкало сверху экрна
            headerView.visibility = View.INVISIBLE
        }
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        initShadow(parent.context)
        headerItem ?: return

        val children = parent.getSortedChildren()
        drawShadows(children, canvas, parent)
        drawHeader(children, canvas, parent)
    }

    private fun drawHeader(children: Map<Int, View>, canvas: Canvas, parent: RecyclerView) {
        if (!::background.isInitialized) {
            background = ShapeDrawable()
            backgroundGrey = ContextCompat.getColor(parent.context, R.color.item_explorer_background)
            backgroundClear = parent.context.findColorByAttr(R.attr.colorBackground)
        }

        when {
            headerPosition == UNDEFINED -> return
            headerView.visibility == View.GONE -> return
            headerView.visibility == View.INVISIBLE -> headerView.visibility = View.VISIBLE
        }

        val headerItemView = children[headerPosition]
        var top = max(0, headerItemView?.top ?: 0)
        if (top > 0) {
            top = -headerView.measuredHeight
        }
        headerView.top = top
        headerView.bottom = top + headerView.measuredHeight

        background.paint.color = when {
            headerPosition % 2 == 0 -> backgroundGrey
            else -> backgroundClear
        }
        background.setBounds(headerView.left, headerView.top, headerView.right, headerView.bottom)
        background.draw(canvas)
    }

    private fun drawShadows(children: Map<Int, View>, canvas: Canvas, parent: RecyclerView) {
        val currentDir = headerItem ?: return
        var currentIndex = UNDEFINED
        var lastIndex = UNDEFINED
        children.forEach {
            val index = it.key
            val item = items[index]
            if (item == currentDir) {
                currentIndex = index
            }
            val nextItem = if (items.size == index.inc()) null else items[index.inc()]
            if (nextItem != null && !currentDir.hasChild(nextItem) && currentDir.hasChild(item)) {
                lastIndex = index
            }
        }

        var child = children[lastIndex]
        if (child != null) {
            drawForLastChild(canvas, child, parent)
        }

        child = children[currentIndex]
        val currentIsNullOrEmpty = currentDir.children.isNullOrEmpty()
        if (child != null) {
            val dynamicOffset = child.bottom * shadowSize / parent.measuredHeight
            when {
                currentIsNullOrEmpty -> drawForEmpty(canvas, child, parent, dynamicOffset, drawTop = child.top >= 0)
                child.top >= 0 -> drawTop(canvas, child, dynamicOffset)
            }
        }

        child = children[currentIndex] ?: children.iterator().next().value
        if (currentIndex == -1 || child.top < 0) {
            drawTopPinned(canvas, child)
        }
    }

    private fun drawForLastChild(canvas: Canvas, child: View, parent: RecyclerView) {
        val rect = Rect()
        parent.getDecoratedBoundsWithMargins(child, rect)
        drawBottom(canvas, rect)
    }

    private fun drawForEmpty(canvas: Canvas, child: View, parent: RecyclerView, dynamicOffset: Int, drawTop: Boolean) {
        val rect = Rect()
        parent.getDecoratedBoundsWithMargins(child, rect)
        drawBottom(canvas, rect)
        if (drawTop) {
            drawTop(canvas, child, dynamicOffset)
        }
    }

    private fun drawTop(canvas: Canvas, child: View, dynamicOffset: Int) {
        topShadow.setBounds(child.left, child.bottom, child.right, child.bottom + shadowSize + dynamicOffset)
        topShadow.draw(canvas)
    }

    private fun drawBottom(canvas: Canvas, rect: Rect) {
        bottomShadow.setBounds(rect.left, rect.bottom - shadowSize, rect.right, rect.bottom)
        bottomShadow.draw(canvas)
    }

    private fun drawTopPinned(canvas: Canvas, anyChild: View) {
        val top = topLimit
        topShadow.setBounds(anyChild.left, top, anyChild.right, top + shadowSize)
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