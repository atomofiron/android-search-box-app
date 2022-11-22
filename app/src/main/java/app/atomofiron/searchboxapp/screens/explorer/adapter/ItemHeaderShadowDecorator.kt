package app.atomofiron.searchboxapp.screens.explorer.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.explorer.adapter.ItemBackgroundDecorator.Companion.getExplorerItemBackground
import app.atomofiron.searchboxapp.screens.explorer.adapter.util.getSortedChildren
import app.atomofiron.searchboxapp.utils.Explorer.hasChild
import kotlin.math.min

class ItemHeaderShadowDecorator(private val getItems: () -> List<Node>) : RecyclerView.ItemDecoration() {
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
    private var headerItem: Node? = null

    private var backgroundGrey = 0
    private var backgroundColor = 0

    fun onHeaderChanged(item: Node?) {
        headerItem = item
    }

    fun setHeaderView(headerView: ExplorerHeaderView) {
        this.headerView = headerView
        initShadow(headerView.context)
        setHeaderBackground()
        backgroundColor = headerView.context.findColorByAttr(R.attr.colorBackground)
        backgroundGrey = headerView.context.getExplorerItemBackground()
        backgroundGrey = ColorUtils.compositeColors(backgroundGrey, backgroundColor)
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        initShadow(parent.context)
        val headerItem = headerItem ?: return
        val position = getItems().indexOfFirst { it.uniqueId == headerItem.uniqueId }
        if (position != headerItemPosition) {
            headerItemPosition = position
            setHeaderBackground()
        }

        val children = parent.getSortedChildren()
        bindHeader(headerItem, children)
        drawShadows(children, canvas, parent)
    }

    private fun bindHeader(headerItem: Node, children: Map<Int, View>) {
        when {
            headerItemPosition == UNDEFINED -> return
            headerView.isGone -> return
        }
        val items = getItems()

        val headerItemView = children[headerItemPosition]
        val firstVisiblePosition = children.keys.first()
        val notChildPosition = children.keys.find { it > headerItemPosition && !headerItem.hasChild(items[it]) } ?: UNDEFINED
        val childPosition = children.keys.find { headerItem.hasChild(items[it]) } ?: UNDEFINED
        val top = when {
            headerItem.children.isNullOrEmpty() -> -headerHeight
            (headerItemView?.top ?: 0) >= topEdge -> -headerHeight
            notChildPosition > childPosition -> {
                val bottom = children[notChildPosition.dec()]?.bottom ?: 0
                min(0, bottom - headerHeight)
            }
            firstVisiblePosition == headerItemPosition -> 0
            firstVisiblePosition == childPosition -> 0
            headerItemView == null -> -headerHeight
            else -> 0
        }

        headerView.top = top
        headerView.bottom = top + headerHeight
    }

    private fun setHeaderBackground() {
        val color = when (headerItemPosition % 2) {
            0 -> backgroundGrey
            else -> backgroundColor
        }
        headerView.setBackgroundColor(color)
    }

    private fun drawShadows(children: Map<Int, View>, canvas: Canvas, parent: RecyclerView) {
        val currentDir = headerItem ?: return
        var lastIndex = UNDEFINED
        val items = getItems()
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
                currentIsNullOrEmpty -> drawForEmpty(canvas, child, parent, dynamicOffset)
                child.top >= topEdge -> drawTop(canvas, child, dynamicOffset)
            }
        }

        child = child ?: children.values.first()
        val content = currentDir.children
        when {
            content.isNullOrEmpty() -> Unit
            children.keys.first() > headerItemPosition + content.size -> Unit
            child.top < topEdge -> drawTopPinned(canvas, child)
        }
    }

    private fun drawForLastChild(canvas: Canvas, child: View, parent: RecyclerView) {
        val rect = Rect()
        parent.getDecoratedBoundsWithMargins(child, rect)
        drawBottom(canvas, rect)
    }

    private fun drawForEmpty(canvas: Canvas, child: View, parent: RecyclerView, dynamicOffset: Int) {
        val rect = Rect()
        parent.getDecoratedBoundsWithMargins(child, rect)
        drawBottom(canvas, rect)
        drawTop(canvas, child, dynamicOffset)
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