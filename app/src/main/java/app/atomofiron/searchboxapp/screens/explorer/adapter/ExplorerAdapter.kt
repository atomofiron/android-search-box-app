package app.atomofiron.searchboxapp.screens.explorer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeAction
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.adapter.ItemSeparationDecorator.Separation
import app.atomofiron.searchboxapp.screens.explorer.adapter.ItemSpaceDecorator.Divider
import app.atomofiron.searchboxapp.screens.explorer.adapter.util.NodeCallback
import java.util.LinkedList

class ExplorerAdapter : ListAdapter<Node, ExplorerHolder>(NodeCallback()) {
    companion object {
        private const val UNDEFINED = -1
        private const val VIEW_TYPE = 1
        private const val VIEW_POOL_MAX_COUNT = 30
    }

    lateinit var itemActionListener: ExplorerItemActionListener
    private var viewPool: Array<View?>? = null
    private var recyclerView: RecyclerView? = null

    private var currentDir: Node? = null
    private lateinit var composition: ExplorerItemComposition
    private val headerItemPosition: Int get() = when (val dir = currentDir) {
        null -> UNDEFINED
        else -> currentList.indexOfFirst { it.uniqueId == dir.uniqueId }
    }

    private val gravityDecorator = ItemGravityDecorator()
    private val backgroundDecorator = ItemBackgroundDecorator()

    init {
        setHasStableIds(true)
    }

    private fun getFirstChild(): View? {
        val recyclerView = recyclerView ?: return null
        return recyclerView.getChildAt(0)!!
    }

    private fun getLastChild(): View? {
        val recyclerView = recyclerView ?: return null
        return recyclerView.getChildAt(recyclerView.childCount.dec())!!
    }

    private val spaceDecorator = ItemSpaceDecorator { i ->
        val items = currentList
        val item = items[i]
        val nextPosition = i.inc()
        when {
            item.isOpened && item.children.isNullOrEmpty() -> Divider.BIG
            item.isOpened -> Divider.SMALL
            nextPosition >= items.size -> Divider.NO // не делаем отступ у последнего элемента
            item.isRoot && items[nextPosition].isRoot -> Divider.NO
            nextPosition == items.size && item.parentPath == currentDir?.path -> Divider.SMALL
            item.rootId != items[nextPosition].rootId -> Divider.SMALL
            nextPosition != items.size && item.parentPath != items[nextPosition].parentPath -> Divider.SMALL
            else -> Divider.NO
        }
    }

    private val shadowDecorator = ItemHeaderShadowDecorator { currentList }

    private val separationDecorator = ItemSeparationDecorator { position ->
        val currentDir = currentDir ?: return@ItemSeparationDecorator Separation.NO
        val items = currentList
        val item = items[position]
        val nextPosition = position.inc()

        when {
            item.path == currentDir.path -> Separation.NO
            item.parentPath == currentDir.path -> Separation.NO
            item.rootId != currentDir.rootId -> Separation.NO
            item.path == currentDir.path -> Separation.NO
            item.isOpened && item.children.isNullOrEmpty() -> Separation.NO
            item.isOpened -> Separation.TOP
            nextPosition == items.size -> Separation.NO // не рисуем под последним элементом
            item.isRoot && items[nextPosition].isRoot -> Separation.NO
            currentDir.isRoot -> Separation.NO
            item.rootId != items[nextPosition].rootId -> Separation.BOTTOM
            item.parentPath != items[nextPosition].parentPath -> Separation.BOTTOM
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

    fun setCurrentDir(dir: Node?) {
        currentDir = dir
    }

    fun setComposition(composition: ExplorerItemComposition) {
        this.composition = composition
        backgroundDecorator.enabled = composition.visibleBg
        notifyDataSetChanged()
    }

    fun scrollToCurrentDir(unit: Unit = Unit) {
        val dir = currentDir ?: return
        var lastChild = getLastChild() ?: return
        val recyclerView = recyclerView!!
        val position = currentList.indexOf(dir)
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

    override fun getItemId(position: Int): Long = currentList[position].hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExplorerHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = getNewView(inflater, parent)
        return ExplorerHolder(view)
    }

    override fun onBindViewHolder(holder: ExplorerHolder, position: Int) {
        val item = getItem(position)
        holder.onBind(item)
        holder.setOnItemActionListener(itemActionListener)
        holder.bindComposition(composition)
    }

    override fun onViewAttachedToWindow(holder: ExplorerHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder.bindingAdapterPosition > 0) {
            itemActionListener.onItemVisible(getItem(holder.bindingAdapterPosition))
        }
    }

    private val actions = LinkedList<NodeAction>()

    fun onAction(action: NodeAction) {
        actions.add(action)
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