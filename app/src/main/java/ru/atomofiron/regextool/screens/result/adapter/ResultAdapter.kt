package ru.atomofiron.regextool.screens.result.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralAdapter
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.model.finder.FinderResult
import ru.atomofiron.regextool.model.preference.ExplorerItemComposition
import ru.atomofiron.regextool.screens.explorer.adapter.ItemBackgroundDecorator
import ru.atomofiron.regextool.screens.explorer.adapter.ItemGravityDecorator

class ResultAdapter : GeneralAdapter<ResultsHolder, FinderResult>() {
    override val useDiffUtils = true
    lateinit var itemActionListener: ResultItemActionListener

    private lateinit var composition: ExplorerItemComposition

    private val gravityDecorator = ItemGravityDecorator()
    private val backgroundDecorator = ItemBackgroundDecorator()

    fun setComposition(composition: ExplorerItemComposition) {
        this.composition = composition
        backgroundDecorator.enabled = composition.visibleBg
    }

    override fun getDiffUtilCallback(old: List<FinderResult>, new: List<FinderResult>): DiffUtil.Callback? {
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
        val itemView = inflater.inflate(R.layout.item_explorer, parent, false)
        return ResultsHolder(itemView)
    }

    override fun onBindViewHolder(holder: ResultsHolder, position: Int) {
        holder.setOnItemActionListener(itemActionListener)
        super.onBindViewHolder(holder, position)
        holder.bindComposition(composition)
    }
}