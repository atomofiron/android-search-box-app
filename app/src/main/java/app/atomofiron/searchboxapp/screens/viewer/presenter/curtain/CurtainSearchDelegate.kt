package app.atomofiron.searchboxapp.screens.viewer.presenter.curtain

import android.view.LayoutInflater
import android.view.ViewGroup
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.databinding.CurtainTextViewerSearchBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapter
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.viewer.TextViewerViewState
import kotlinx.coroutines.CoroutineScope
import lib.atomofiron.android_window_insets_compat.applyPaddingInsets

class CurtainSearchDelegate(
    output: FinderAdapterOutput,
    viewState: TextViewerViewState,
    scope: CoroutineScope,
) : CurtainApi.Adapter<CurtainApi.ViewHolder>() {

    private val node: Node = viewState.item
    private val composition = viewState.composition

    private val finderAdapter = FinderAdapter()

    init {
        finderAdapter.output = output
        viewState.searchItems.collect(scope, collector = finderAdapter::submitList)
    }

    override fun getHolder(inflater: LayoutInflater, container: ViewGroup, layoutId: Int): CurtainApi.ViewHolder {
        val binding = CurtainTextViewerSearchBinding.inflate(inflater, container, false)

        val holder = ExplorerHolder(binding.itemExplorer.root)
        holder.bind(node)
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
}