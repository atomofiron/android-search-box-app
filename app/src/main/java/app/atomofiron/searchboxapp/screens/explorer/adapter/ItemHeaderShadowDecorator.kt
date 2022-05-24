package app.atomofiron.searchboxapp.screens.explorer.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.screens.explorer.adapter.util.getSortedChildren
import kotlin.math.min

class ItemHeaderShadowDecorator(private val items: List<XFile>) : RecyclerView.ItemDecoration() {
    companion object {
        private const val UNDEFINED = -1
        private const val SHADOW_ALPHA = 100
    }

    private lateinit var topShadow: Drawable
    private lateinit var bottomShadow: Drawable
    private var shadowSize = 0

    private lateinit var headerView: ExplorerHeaderView
    private val headerHeight: Int get() = headerView.measuredHeight
    private val topEdge: Int get() = headerView.paddingTop

    private var headerItemPosition: Int = UNDEFINED
    private var headerItem: XFile? = null

    private lateinit var background: ShapeDrawable
    private var backgroundGrey = 0
    private var backgroundColor = 0

    fun onHeaderChanged(item: XFile?, position: Int) {
        headerItem = item
        headerItemPosition = position
    }

    fun setHeaderView(headerView: ExplorerHeaderView) {
        this.headerView = headerView
        initShadow(headerView.context)
        background = ShapeDrawable()
        backgroundColor = headerView.context.findColorByAttr(R.attr.colorBackground)
        backgroundGrey = ContextCompat.getColor(headerView.context, R.color.item_explorer_background)
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        initShadow(parent.context)
        val headerItem = headerItem ?: return

        val children = parent.getSortedChildren()
        bindHeader(headerItem, children)
        drawShadows(children, canvas, parent)
        drawHeaderBackground(canvas)
    }

    private fun bindHeader(headerItem: XFile, children: Map<Int, View>) {
        when {
            headerItemPosition == UNDEFINED -> return
            headerView.isGone -> return
        }

        val firstVisiblePosition = children.keys.first()
        val notChildPosition = children.keys.find { !headerItem.hasChild(items[it]) } ?: UNDEFINED
        val childPosition = children.keys.find { headerItem.hasChild(items[it]) } ?: Int.MAX_VALUE
        val top = when {
            (children[headerItemPosition]?.top ?: 0) >= topEdge -> -headerHeight
            notChildPosition > childPosition -> {
                val itemView = children.getValue(notChildPosition)
                min(0, itemView.top - headerHeight)
            }
            firstVisiblePosition == headerItemPosition -> when {
                headerItem.children.isNullOrEmpty() -> {
                    val itemView = children.getValue(firstVisiblePosition.inc())
                    min(0, itemView.top - headerHeight)
                }
                else -> 0
            }
            firstVisiblePosition == childPosition -> 0
            else -> -headerHeight
        }

        headerView.top = top
        headerView.bottom = top + headerHeight
    }

    private fun drawHeaderBackground(canvas: Canvas) {
        background.setBounds(headerView.left, headerView.top, headerView.right, headerView.bottom)
        background.paint.color = backgroundColor
        background.draw(canvas)
        if (headerItemPosition % 2 == 0) {
            background.paint.color = backgroundGrey
            background.draw(canvas)
        }
    }

    private fun drawShadows(children: Map<Int, View>, canvas: Canvas, parent: RecyclerView) {
        val currentDir = headerItem ?: return
        var lastIndex = UNDEFINED
        children.forEach {
            val index = it.key
            val item = items[index]
            val nextItem = if (items.size == index.inc()) null else items[index.inc()]
            if (nextItem != null && !currentDir.hasChild(nextItem) && currentDir.hasChild(item)) {
                lastIndex = index
            }
        }

        var child = children[lastIndex]
        if (child != null) {
            drawForLastChild(canvas, child, parent)
        }

        child = children[headerItemPosition]
        val currentIsNullOrEmpty = currentDir.children.isNullOrEmpty()
        if (child != null) {
            val dynamicOffset = child.bottom * shadowSize / parent.measuredHeight
            when {
                currentIsNullOrEmpty -> drawForEmpty(canvas, child, parent, dynamicOffset, drawTop = child.top > 0)
                child.top >= topEdge -> drawTop(canvas, child, dynamicOffset)
            }
        }

        child = child ?: children.values.first()
        if (child.top < topEdge) {
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
        val top = headerView.bottom
        topShadow.setBounds(anyChild.left, top, anyChild.right, top + shadowSize)
        topShadow.draw(canvas)
    }

    private fun initShadow(context: Context) {
        topShadow = ContextCompat.getDrawable(context, R.drawable.item_explorer_opened_dir_shadow_top)!!
        bottomShadow = ContextCompat.getDrawable(context, R.drawable.item_explorer_opened_dir_shadow_bottom)!!
        topShadow.alpha = SHADOW_ALPHA
        bottomShadow.alpha = SHADOW_ALPHA
        shadowSize = context.resources.getDimensionPixelSize(R.dimen.item_dir_shadow_size)
    }
}