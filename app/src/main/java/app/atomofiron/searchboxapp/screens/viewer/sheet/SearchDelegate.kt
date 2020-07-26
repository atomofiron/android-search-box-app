package app.atomofiron.searchboxapp.screens.viewer.sheet

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.bottom_sheet.BottomSheetDelegate
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapter
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

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
        recyclerView.itemAnimator = null
    }

    override fun onViewShown() {
    }

    fun setItems(items: List<FinderStateItem>) {
        finderAdapter.setItems(items)
    }
}