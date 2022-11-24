package app.atomofiron.searchboxapp.screens.explorer

import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.FragmentExplorerBinding
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.custom.OrientationLayoutDelegate
import app.atomofiron.searchboxapp.screens.explorer.adapter.*
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerHeaderDelegate
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerListDelegate
import app.atomofiron.searchboxapp.screens.explorer.fragment.SwipeMarkerDelegate
import app.atomofiron.searchboxapp.screens.explorer.places.PlacesAdapter
import app.atomofiron.searchboxapp.screens.main.util.KeyCodeConsumer
import app.atomofiron.searchboxapp.setContentMaxWidthRes
import app.atomofiron.searchboxapp.utils.getString
import com.google.android.material.snackbar.Snackbar
import lib.atomofiron.android_window_insets_compat.applyPaddingInsets
import lib.atomofiron.android_window_insets_compat.insetsProxying

class ExplorerFragment : Fragment(R.layout.fragment_explorer),
    BaseFragment<ExplorerFragment, ExplorerViewState, ExplorerPresenter> by BaseFragmentImpl(),
    KeyCodeConsumer
{
    private lateinit var binding: FragmentExplorerBinding

    private val explorerAdapter = ExplorerAdapter()
    private val placesAdapter = PlacesAdapter()

    private lateinit var listDelegate: ExplorerListDelegate
    private lateinit var headerDelegate: ExplorerHeaderDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, ExplorerViewModel::class, savedInstanceState)

        explorerAdapter.itemActionListener = presenter
        placesAdapter.itemActionListener = presenter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentExplorerBinding.bind(view)

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = explorerAdapter
        binding.recyclerView.addOnItemTouchListener(SwipeMarkerDelegate(resources))

        binding.bottomBar.setContentMaxWidthRes(R.dimen.bottom_bar_max_width)
        binding.bottomBar.isItemActiveIndicatorEnabled = false
        binding.bottomBar.setOnItemSelectedListener(::onNavigationItemSelected)
        binding.navigationRail.menu.removeItem(R.id.stub)
        binding.navigationRail.setOnItemSelectedListener(::onNavigationItemSelected)
        binding.navigationRail.isItemActiveIndicatorEnabled = false

        binding.run {
            OrientationLayoutDelegate(coordinator, recyclerView, bottomBar, navigationRail, explorerHeader)
        }

        binding.verticalDock.run {
            onGravityChangeListener = presenter::onDockGravityChange
            recyclerView.adapter = placesAdapter
        }
        listDelegate = ExplorerListDelegate(binding.recyclerView, explorerAdapter, binding.explorerHeader, presenter)
        headerDelegate = ExplorerHeaderDelegate(binding.recyclerView, binding.explorerHeader, explorerAdapter)

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
        viewCollect(current) {
            listDelegate.setCurrentDir(it)
            headerDelegate.setCurrentDir(it)
        }
        viewCollect(itemComposition) {
            listDelegate.setComposition(it)
            headerDelegate.setComposition(it)
            explorerAdapter.setComposition(it)
        }
        viewCollect(permissionRequiredWarning, collector = ::showPermissionRequiredWarning)
        viewCollect(historyDrawerGravity) { binding.verticalDock.gravity = it }
        viewCollect(places, collector = placesAdapter::setItems)
        viewCollect(scrollToCurrentDir, collector = listDelegate::scrollToCurrentDir)
        viewCollect(alerts, collector = ::showAlert)
    }

    override fun onApplyInsets(root: View) {
        root.insetsProxying()
        // binding.coordinator is already in OrientationLayoutDelegate
        binding.verticalDock.insetsProxying()
        binding.recyclerView.applyPaddingInsets()
        binding.explorerHeader.applyPaddingInsets()
        binding.bottomBar.applyPaddingInsets(start = true, bottom = true, end = true)
        binding.navigationRail.applyPaddingInsets()
    }

    override fun onBack(): Boolean {
        if (binding.verticalDock.isOpened) {
            binding.verticalDock.close()
            return true
        }
        return super.onBack()
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