package ru.atomofiron.regextool.screens.explorer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralAdapter
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.ExplorerHeaderView
import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.model.preference.ExplorerItemComposition
import ru.atomofiron.regextool.screens.explorer.adapter.ItemSeparationDecorator.Separation
import ru.atomofiron.regextool.screens.explorer.adapter.ItemSpaceDecorator.Divider

class ExplorerAdapter : GeneralAdapter<ExplorerHolder, XFile>() {
    companion object {
        private const val UNDEFINED = -1
        private const val VIEW_TYPE = 1
        private const val VIEW_POOL_MAX_COUNT = 30
    }

    lateinit var itemActionListener: ExplorerItemActionListener
    private var viewPool: Array<View?>? = null
    private var recyclerView: RecyclerView? = null

    private var currentDir: XFile? = null
    private lateinit var composition: ExplorerItemComposition
    private lateinit var headerView: ExplorerHeaderView
    private var headerItemPosition: Int = UNDEFINED

    private val gravityDecorator = ItemGravityDecorator()
    private val backgroundDecorator = ItemBackgroundDecorator()

    private fun getFirstChild(): View? {
        val recyclerView = recyclerView ?: return null
        return recyclerView.getChildAt(0)!!
    }

    private fun getLastChild(): View? {
        val recyclerView = recyclerView ?: return null
        return recyclerView.getChildAt(recyclerView.childCount.dec())!!
    }

    private val spaceDecorator = ItemSpaceDecorator { i ->
        val item = items[i]
        val nextPosition = i.inc()
        when {
            item.isOpened && item.children.isNullOrEmpty() -> Divider.BIG
            item.isOpened -> Divider.SMALL
            nextPosition >= items.size -> Divider.NO // не делаем отступ у последнего элемента
            item.isRoot && items[nextPosition].isRoot -> Divider.NO
            nextPosition == items.size && item.completedParentPath == currentDir?.completedPath -> Divider.SMALL
            item.root != items[nextPosition].root -> Divider.SMALL
            nextPosition != items.size && item.completedParentPath != items[nextPosition].completedParentPath -> Divider.SMALL
            else -> Divider.NO
        }
    }

    private val shadowDecorator = ItemHeaderShadowDecorator(items)

    private val separationDecorator = ItemSeparationDecorator { position ->
        val currentDir = currentDir ?: return@ItemSeparationDecorator Separation.NO
        val item = items[position]
        val nextPosition = position.inc()

        when {
            item.completedPath == currentDir.completedPath -> Separation.NO
            item.completedParentPath == currentDir.completedPath -> Separation.NO
            item.root != currentDir.root -> Separation.NO
            item.completedPath == currentDir.completedPath -> Separation.NO
            item.isOpened && item.children.isNullOrEmpty() -> Separation.NO
            item.isOpened -> Separation.TOP
            nextPosition == items.size -> Separation.NO // не рисуем под последним элементом
            item.isRoot && items[nextPosition].isRoot -> Separation.NO
            currentDir.isRoot -> Separation.NO
            item.root != items[nextPosition].root -> Separation.BOTTOM
            item.completedParentPath != items[nextPosition].completedParentPath -> Separation.BOTTOM
            else -> Separation.NO
        }
    }

    fun isCurrentDirVisible(): Boolean {
        val firstChild = getFirstChild() ?: return false
        val recyclerView = recyclerView!!
        val topItemPosition = recyclerView.getChildLayoutPosition(firstChild)
        val bottomItemPosition = recyclerView.getChildLayoutPosition(getLastChild()!!)
        return headerItemPosition in topItemPosition..bottomItemPosition
    }

    fun setCurrentDir(dir: XFile?) {
        currentDir = dir
        headerItemPosition = items.indexOf(dir)
        headerView.onBind(dir)
        shadowDecorator.onHeaderChanged(dir, headerItemPosition)
    }

    fun setHeaderView(view: ExplorerHeaderView) {
        headerView = view
        if (::composition.isInitialized) {
            view.setComposition(composition)
        }
        view.onBind(currentDir)
        shadowDecorator.setHeaderView(view)
    }

    fun setComposition(composition: ExplorerItemComposition) {
        this.composition = composition
        headerView.setComposition(composition)
        backgroundDecorator.enabled = composition.visibleBg
        notifyItemRangeChanged(0, items.size)
    }

    fun scrollToCurrentDir() {
        val dir = currentDir ?: return
        var lastChild = getLastChild() ?: return
        val recyclerView = recyclerView!!
        val position = items.indexOf(dir)
        val lastItemPosition = recyclerView.getChildLayoutPosition(lastChild)
        when {
            position > lastItemPosition -> {
                recyclerView.scrollToPosition(position.dec())
                recyclerView.post {
                    lastChild = getLastChild()!!
                    recyclerView.smoothScrollBy(0, lastChild.height * 3 / 2) // ItemSpaceDecorator
                }
            }
            else -> {
                recyclerView.scrollToPosition(position.inc())
                recyclerView.post {
                    val firstChild = getFirstChild()!!
                    recyclerView.smoothScrollBy(0, -firstChild.height * 5 / 2) // ItemSpaceDecorator
                }
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        recyclerView.itemAnimator = null
        recyclerView.recycledViewPool.setMaxRecycledViews(VIEW_TYPE, VIEW_POOL_MAX_COUNT)

        viewPool = arrayOfNulls(VIEW_POOL_MAX_COUNT)
        val inflater = LayoutInflater.from(recyclerView.context)
        for (i in viewPool!!.indices) {
            viewPool!![i] = inflateNewView(inflater, recyclerView)
        }
        separationDecorator.onAttachedToRecyclerView(recyclerView.context)

        recyclerView.addItemDecoration(backgroundDecorator)
        recyclerView.addItemDecoration(spaceDecorator)
        recyclerView.addItemDecoration(separationDecorator)
        recyclerView.addItemDecoration(shadowDecorator)
        recyclerView.addItemDecoration(gravityDecorator)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null

        recyclerView.removeItemDecoration(backgroundDecorator)
        recyclerView.removeItemDecoration(spaceDecorator)
        recyclerView.removeItemDecoration(separationDecorator)
        recyclerView.removeItemDecoration(shadowDecorator)
        recyclerView.removeItemDecoration(gravityDecorator)
    }

    override fun getItemViewType(position: Int): Int = VIEW_TYPE

    override fun getItemId(position: Int): Long = items[position].hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): ExplorerHolder {
        val view = getNewView(inflater, parent)
        return ExplorerHolder(view)
    }

    override fun onBindViewHolder(holder: ExplorerHolder, position: Int) {
        holder.setOnItemActionListener(itemActionListener)
        super.onBindViewHolder(holder, position)
        holder.bindComposition(composition)
        itemActionListener.onItemVisible(holder.item)
        if (position == headerItemPosition) {
            headerView.onBind(items[position])
        }
    }

    override fun setItem(item: XFile) {
        super.setItem(item)
        if (headerItemPosition == UNDEFINED) {
            return
        }
        val headerItem = items[headerItemPosition]
        if (item == headerItem) {
            headerView.onBind(item)
        }
    }

    override fun onViewDetachedFromWindow(holder: ExplorerHolder) {
        super.onViewDetachedFromWindow(holder)
        itemActionListener.onItemInvalidate(holder.item)
    }

    private fun getNewView(inflater: LayoutInflater, parent: ViewGroup): View {
        val viewPool = viewPool
        if (viewPool != null) {
            for (i in viewPool.indices) {
                val view = viewPool[i]
                if (view != null) {
                    viewPool[i] = null
                    return view
                }
            }
            this.viewPool = null
        }
        return inflateNewView(inflater, parent)
    }

    private fun inflateNewView(inflater: LayoutInflater, parent: ViewGroup): View {
        return inflater.inflate(R.layout.item_explorer, parent, false)
    }
}