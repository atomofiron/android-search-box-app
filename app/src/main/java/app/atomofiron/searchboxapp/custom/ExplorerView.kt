package app.atomofiron.searchboxapp.custom

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.ViewExplorerBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.Node.Companion.toUniqueId
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.model.explorer.NodeTabItems
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerListDelegate
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerSpanSizeLookup
import app.atomofiron.searchboxapp.screens.explorer.fragment.SwipeMarkerDelegate
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerAdapter
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerItemActionListener
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.OnScrollIdleSubmitter
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter
import app.atomofiron.searchboxapp.scrollToTop
import app.atomofiron.searchboxapp.utils.Tool.endingDot
import lib.atomofiron.android_window_insets_compat.applyPaddingInsets

@SuppressLint("ViewConstructor")
class ExplorerView(
    context: Context,
    private val output: ExplorerViewOutput,
) : FrameLayout(context) {

    private val binding = ViewExplorerBinding.inflate(LayoutInflater.from(context), this)
    private val rootAliases = HashMap<Int, String>()

    val recyclerView = binding.recyclerView
    val headerView = binding.explorerHeader
    val systemUiView = binding.systemUiBackground

    private val rootAdapter = RootAdapter(rootAliases)
    private val explorerAdapter = ExplorerAdapter(rootAliases)

    private lateinit var listDelegate: ExplorerListDelegate
    private lateinit var spanSizeLookup: ExplorerSpanSizeLookup

    private val submitter = OnScrollIdleSubmitter(binding.recyclerView, explorerAdapter)

    init {
        explorerAdapter.itemActionListener = output
        explorerAdapter.separatorClickListener = ::onSeparatorClick
        rootAdapter.clickListener = output

        binding.applyInsets()
        binding.init()
    }

    private fun ViewExplorerBinding.init() {
        val layoutManager = GridLayoutManager(context, 1)
        spanSizeLookup = ExplorerSpanSizeLookup(recyclerView, layoutManager, rootAdapter)
        layoutManager.spanSizeLookup = spanSizeLookup
        recyclerView.layoutManager = layoutManager
        val config = ConcatAdapter.Config.Builder()
            .setStableIdMode(ConcatAdapter.Config.StableIdMode.SHARED_STABLE_IDS)
            .build()
        recyclerView.adapter = ConcatAdapter(config, rootAdapter, explorerAdapter)
        recyclerView.addOnItemTouchListener(SwipeMarkerDelegate(resources))

        listDelegate = ExplorerListDelegate(
            recyclerView,
            rootAdapter,
            explorerAdapter,
            explorerHeader,
            output,
            rootAliases,
        )
    }

    private fun ViewExplorerBinding.applyInsets() {
        recyclerView.applyPaddingInsets()
        explorerHeader.applyPaddingInsets(start = true, top = true, end = true)
    }

    fun onInsetsApplied() = spanSizeLookup.updateSpanCount(recyclerView)

    fun scrollTo(item: Node) = listDelegate.scrollTo(item)

    fun scrollToTop() = recyclerView.scrollToTop()

    fun isCurrentDirVisible(): Boolean? = listDelegate.isCurrentDirVisible()

    fun submitList(items: NodeTabItems) {
        initRootAliases(items.roots)
        rootAdapter.submitList(items.roots)
        submitter.trySubmitList(items.items, items.current?.path)
        listDelegate.setCurrentDir(items.current)
    }

    fun setComposition(composition: ExplorerItemComposition) {
        listDelegate.setComposition(composition)
        explorerAdapter.setComposition(composition)
    }

    private fun onSeparatorClick(item: Node) = when {
        listDelegate.isVisible(item) -> Unit
        else -> listDelegate.scrollTo(item)
    }

    private fun initRootAliases(roots: List<NodeRoot>) {
        if (rootAliases.isNotEmpty()) return
        for (root in roots) {
            when (root.type) {
                is NodeRoot.NodeRootType.Photos -> addRootAlias(root.item.path, R.string.root_photos)
                is NodeRoot.NodeRootType.Videos -> addRootAlias(root.item.path, R.string.root_photos)
                is NodeRoot.NodeRootType.Camera -> addRootAlias(root.item.path, R.string.root_camera)
                is NodeRoot.NodeRootType.Downloads -> addRootAlias(root.item.path, R.string.root_downloads)
                is NodeRoot.NodeRootType.Bluetooth -> addRootAlias(root.item.path, R.string.root_bluetooth)
                is NodeRoot.NodeRootType.Screenshots -> addRootAlias(root.item.path, R.string.root_screenshots)
                is NodeRoot.NodeRootType.InternalStorage -> addRootAlias(root.item.path, R.string.internal_storage)
                is NodeRoot.NodeRootType.Favorite -> Unit
            }
        }
    }

    private fun addRootAlias(path: String, @StringRes alias: Int) {
        val string = resources.getString(alias)
        rootAliases[path.toUniqueId()] = string
        rootAliases[path.endingDot().toUniqueId()] = string
    }

    interface ExplorerViewOutput : ExplorerItemActionListener, RootAdapter.RootClickListener
}