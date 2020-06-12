package ru.atomofiron.regextool.screens.viewer.sheet

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.bottom_sheet.BottomSheetDelegate
import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.model.preference.ExplorerItemComposition
import ru.atomofiron.regextool.screens.explorer.adapter.ExplorerHolder
import ru.atomofiron.regextool.screens.finder.adapter.FinderAdapter
import ru.atomofiron.regextool.screens.finder.adapter.FinderAdapterOutput
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem

class SearchDelegate(private val output: FinderAdapterOutput) : BottomSheetDelegate(R.layout.sheet_viewer_search) {
    private val itemView: View get() = bottomSheetView.contentView.findViewById(R.id.item_explorer)
    private val recyclerView: RecyclerView get() = bottomSheetView.contentView.findViewById(R.id.sheet_viewer_search_rv)

    private lateinit var xFile: XFile
    private lateinit var composition: ExplorerItemComposition

    private val finderAdapter = FinderAdapter()

    init {
        finderAdapter.output = output
    }

    fun show(xFile: XFile, composition: ExplorerItemComposition) {
        this.xFile = xFile
        this.composition = composition
        super.show()
    }

    override fun onViewReady() {
        val holder = ExplorerHolder(itemView)
        holder.bind(xFile)
        holder.bindComposition(composition)
        holder.removeBackground()
        holder.disableCheckBox()

        recyclerView.adapter = finderAdapter
    }

    override fun onViewShown() {
    }

    fun setItems(items: List<FinderStateItem>) {
        finderAdapter.setItems(items)
    }
}