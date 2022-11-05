package app.atomofiron.searchboxapp.screens.viewer.presenter.curtain

import android.view.LayoutInflater
import android.view.ViewGroup
import app.atomofiron.searchboxapp.databinding.CurtainTextViewerSearchBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapter
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import lib.atomofiron.android_window_insets_compat.applyPaddingInsets

class CurtainSearchDelegate(
    output: FinderAdapterOutput,
) : CurtainApi.Adapter<CurtainApi.ViewHolder>() {

    private lateinit var xFile: Node
    private lateinit var composition: ExplorerItemComposition

    private val finderAdapter = FinderAdapter()

    init {
        finderAdapter.output = output
    }

    override fun getHolder(inflater: LayoutInflater, container: ViewGroup, layoutId: Int): CurtainApi.ViewHolder {
        val binding = CurtainTextViewerSearchBinding.inflate(inflater, container, false)

        val holder = ExplorerHolder(binding.itemExplorer.root)
        holder.bind(xFile)
        holder.bindComposition(composition)
        holder.disableClicks()
        holder.hideCheckBox()
        holder.setGreyBackgroundColor()

        binding.sheetViewerSearchRv.adapter = finderAdapter
        binding.sheetViewerSearchRv.itemAnimator = null

        binding.root.applyPaddingInsets(top = true, withProxying = true)
        binding.sheetViewerSearchRv.applyPaddingInsets(bottom = true)

        return CurtainApi.ViewHolder(binding.root)
    }

    fun set(items: List<FinderStateItem>, xFile: Node, composition: ExplorerItemComposition) {
        finderAdapter.setItems(items)
        this.xFile = xFile
        this.composition = composition
    }
}