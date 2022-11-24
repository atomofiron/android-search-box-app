package app.atomofiron.searchboxapp.screens.result.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.finder.FinderResult
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.adapter.ItemBackgroundDecorator
import app.atomofiron.searchboxapp.screens.explorer.adapter.ItemGravityDecorator

class ResultAdapter : GeneralAdapter<ResultsHolder, FinderResultItem>() {
    companion object {
        private const val POSITION_HEADER = 0
        private const val TYPE_HEADER = 2
    }
    override val useDiffUtils = true
    lateinit var itemActionListener: ResultItemActionListener

    private lateinit var composition: ExplorerItemComposition

    private val gravityDecorator = ItemGravityDecorator()
    private val backgroundDecorator = ItemBackgroundDecorator()

    private val header = FinderResultItem.Header(0, 0)

    fun setResults(results: List<FinderResult>) {
        val items = results.map { FinderResultItem.Item(it) }
        super.setItems(items)

        header.dirsCount = 0
        header.filesCount = 0
        for (item in items) {
            when {
                item.item.isDirectory -> header.dirsCount++
                else -> header.filesCount++
            }
        }
        notifyItemChanged(POSITION_HEADER)
    }

    fun setComposition(composition: ExplorerItemComposition) {
        this.composition = composition
        backgroundDecorator.enabled = composition.visibleBg
    }

    override fun getItemViewType(position: Int): Int = when (position) {
        POSITION_HEADER -> TYPE_HEADER
        else -> super.getItemViewType(position)
    }

    override fun getItemCount(): Int = items.size.inc()

    override fun getDiffUtilCallback(old: List<FinderResultItem>, new: List<FinderResultItem>): DiffUtil.Callback? {
        return ResultDiffUtilCallback(old, new)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.addItemDecoration(gravityDecorator)
        recyclerView.addItemDecoration(backgroundDecorator)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        recyclerView.removeItemDecoration(gravityDecorator)
        recyclerView.removeItemDecoration(backgroundDecorator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): ResultsHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val itemView = inflater.inflate(R.layout.item_header, parent, false)
                ResultsHeaderHolder(itemView)
            }
            else -> {
                val itemView = inflater.inflate(R.layout.item_explorer, parent, false)
                ResultsItemHolder(itemView)
            }
        }
    }

    override fun onBindViewHolder(holder: ResultsHolder, position: Int) = when (position) {
        POSITION_HEADER -> holder.bind(header)
        else -> {
            holder as ResultsItemHolder
            holder.setOnItemActionListener(itemActionListener)
            super.onBindViewHolder(holder, position.dec())
            holder.bindComposition(composition)
            itemActionListener.onItemVisible(items[position.dec()] as FinderResultItem.Item)
        }
    }
}