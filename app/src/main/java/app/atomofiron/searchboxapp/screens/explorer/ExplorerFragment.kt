package app.atomofiron.searchboxapp.screens.explorer

import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.FragmentExplorerBinding
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.custom.OrientationLayoutDelegate
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.Node.Companion.toUniqueId
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.model.explorer.NodeRoot.NodeRootType
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.*
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerListDelegate
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerSpanSizeLookup
import app.atomofiron.searchboxapp.screens.explorer.fragment.SwipeMarkerDelegate
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter
import app.atomofiron.searchboxapp.screens.main.util.KeyCodeConsumer
import app.atomofiron.searchboxapp.setContentMaxWidthRes
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.withoutDot
import app.atomofiron.searchboxapp.utils.Tool.endingDot
import app.atomofiron.searchboxapp.utils.getString
import com.google.android.material.snackbar.Snackbar
import lib.atomofiron.android_window_insets_compat.applyPaddingInsets

class ExplorerFragment : Fragment(R.layout.fragment_explorer),
    BaseFragment<ExplorerFragment, ExplorerViewState, ExplorerPresenter> by BaseFragmentImpl(),
    KeyCodeConsumer
{
    private lateinit var binding: FragmentExplorerBinding
    private val rootAliases = HashMap<Int, String>()

    private val rootAdapter = RootAdapter(rootAliases)
    private val explorerAdapter = ExplorerAdapter(rootAliases)

    private lateinit var listDelegate: ExplorerListDelegate
    private lateinit var spanSizeLookup: ExplorerSpanSizeLookup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, ExplorerViewModel::class, savedInstanceState)

        explorerAdapter.itemActionListener = presenter
        explorerAdapter.separatorClickListener = ::onSeparatorClick
        rootAdapter.clickListener = presenter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentExplorerBinding.bind(view)

        val layoutManager = GridLayoutManager(context, 1)
        spanSizeLookup = ExplorerSpanSizeLookup(binding.recyclerView, layoutManager, rootAdapter)
        layoutManager.spanSizeLookup = spanSizeLookup
        binding.recyclerView.layoutManager = layoutManager
        val config = ConcatAdapter.Config.Builder()
            .setStableIdMode(ConcatAdapter.Config.StableIdMode.SHARED_STABLE_IDS)
            .build()
        binding.recyclerView.adapter = ConcatAdapter(config, rootAdapter, explorerAdapter)
        binding.recyclerView.addOnItemTouchListener(SwipeMarkerDelegate(resources))

        binding.bottomBar.setContentMaxWidthRes(R.dimen.bottom_bar_max_width)
        binding.bottomBar.isItemActiveIndicatorEnabled = false
        binding.bottomBar.setOnItemSelectedListener(::onNavigationItemSelected)
        binding.navigationRail.menu.removeItem(R.id.stub)
        binding.navigationRail.setOnItemSelectedListener(::onNavigationItemSelected)
        binding.navigationRail.isItemActiveIndicatorEnabled = false

        listDelegate = ExplorerListDelegate(
            binding.recyclerView,
            rootAdapter, explorerAdapter,
            binding.explorerHeader,
            presenter,
            rootAliases,
        )

        viewState.onViewCollect()
        onApplyInsets(view)
    }

    private fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_search -> presenter.onSearchOptionSelected()
            R.id.menu_settings -> presenter.onSettingsOptionSelected()
        }
        return false
    }

    override fun ExplorerViewState.onViewCollect() {
        viewCollect(actions, collector = explorerAdapter::onAction)
        viewCollect(items) {
            initRootAliases(it.roots)
            rootAdapter.submitList(it.roots)
            explorerAdapter.submitList(it.items)
            listDelegate.setCurrentDir(it.current)
        }
        viewCollect(itemComposition) {
            listDelegate.setComposition(it)
            explorerAdapter.setComposition(it)
        }
        viewCollect(permissionRequiredWarning, collector = ::showPermissionRequiredWarning)
        viewCollect(scrollTo, collector = listDelegate::scrollTo)
        viewCollect(alerts, collector = ::showAlert)
    }

    override fun onApplyInsets(root: View) {
        binding.recyclerView.applyPaddingInsets()
        binding.explorerHeader.applyPaddingInsets(start = true, top = true, end = true)
        binding.explorerTabs.applyPaddingInsets(start = true, top = true, end = true)
        binding.bottomBar.applyPaddingInsets(start = true, bottom = true, end = true)
        binding.navigationRail.applyPaddingInsets()
        binding.run {
            OrientationLayoutDelegate(
                binding.root,
                recyclerView,
                bottomBar,
                navigationRail,
                systemUiBackground,
                explorerTabs,
                explorerHeader,
            ) {
                spanSizeLookup.updateSpanCount(recyclerView)
            }
        }
    }

    override fun onKeyDown(keyCode: Int): Boolean = when {
        !isVisible -> false
        keyCode != KeyEvent.KEYCODE_VOLUME_UP -> false
        else -> {
            presenter.onVolumeUp(listDelegate.isCurrentDirVisible())
            true
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        explorerAdapter.notifyItemChanged(0)
    }

    private fun onSeparatorClick(item: Node) {
        val path = item.withoutDot()
        val index = explorerAdapter.currentList.indexOfFirst { it.path == path }
        val dir = explorerAdapter.currentList.getOrNull(index)
        dir ?: return
        presenter.onSeparatorClick(dir, listDelegate.isVisible(index))
    }

    private fun initRootAliases(roots: List<NodeRoot>) {
        if (rootAliases.isNotEmpty()) return
        for (root in roots) {
            when (root.type) {
                is NodeRootType.Photos -> addRootAlias(root.item.path, R.string.root_photos)
                is NodeRootType.Videos -> addRootAlias(root.item.path, R.string.root_photos)
                is NodeRootType.Camera -> addRootAlias(root.item.path, R.string.root_camera)
                is NodeRootType.Downloads -> addRootAlias(root.item.path, R.string.root_downloads)
                is NodeRootType.Bluetooth -> addRootAlias(root.item.path, R.string.root_bluetooth)
                is NodeRootType.Screenshots -> addRootAlias(root.item.path, R.string.root_screenshots)
                is NodeRootType.InternalStorage -> addRootAlias(root.item.path, R.string.internal_storage)
                is NodeRootType.Favorite -> Unit
            }
        }
    }

    private fun addRootAlias(path: String, @StringRes alias: Int) {
        val string = resources.getString(alias)
        rootAliases[path.toUniqueId()] = string
        rootAliases[path.endingDot().toUniqueId()] = string
    }

    private fun showPermissionRequiredWarning(unit: Unit) {
        val view = view ?: return
        Snackbar.make(view, R.string.access_to_storage_forbidden, Snackbar.LENGTH_LONG)
            .setAnchorView(view)
            .setAction(R.string.allow) { presenter.onAllowStorageClick() }
            .show()
    }

    private fun showAlert(error: NodeError) {
        val view = view ?: return
        Snackbar.make(view, resources.getString(error), Snackbar.LENGTH_LONG)
            .setAnchorView(view)
            .setAction(R.string.allow) { presenter.onAllowStorageClick() }
            .show()
    }
}