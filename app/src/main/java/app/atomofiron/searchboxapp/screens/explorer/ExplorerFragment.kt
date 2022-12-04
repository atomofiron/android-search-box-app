package app.atomofiron.searchboxapp.screens.explorer

import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
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
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.*
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerListDelegate
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerSpanSizeLookup
import app.atomofiron.searchboxapp.screens.explorer.fragment.SwipeMarkerDelegate
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter
import app.atomofiron.searchboxapp.screens.main.util.KeyCodeConsumer
import app.atomofiron.searchboxapp.setContentMaxWidthRes
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.withoutDot
import app.atomofiron.searchboxapp.utils.getString
import com.google.android.material.snackbar.Snackbar
import lib.atomofiron.android_window_insets_compat.applyPaddingInsets

class ExplorerFragment : Fragment(R.layout.fragment_explorer),
    BaseFragment<ExplorerFragment, ExplorerViewState, ExplorerPresenter> by BaseFragmentImpl(),
    KeyCodeConsumer
{
    private lateinit var binding: FragmentExplorerBinding

    private val rootAdapter = RootAdapter()
    private val explorerAdapter = ExplorerAdapter()

    private lateinit var listDelegate: ExplorerListDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, ExplorerViewModel::class, savedInstanceState)

        explorerAdapter.itemActionListener = presenter
        explorerAdapter.separatorClickListener = ::onSeparatorClick
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentExplorerBinding.bind(view)

        binding.recyclerView.layoutManager = GridLayoutManager(context, 1).apply {
            spanSizeLookup = ExplorerSpanSizeLookup(binding.recyclerView, this, rootAdapter)
        }
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

        listDelegate = ExplorerListDelegate(binding.recyclerView, rootAdapter, explorerAdapter, binding.explorerHeader, presenter)

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
        viewCollect(items, collector = explorerAdapter::submitList)
        viewCollect(current, collector = listDelegate::setCurrentDir)
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
            )
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