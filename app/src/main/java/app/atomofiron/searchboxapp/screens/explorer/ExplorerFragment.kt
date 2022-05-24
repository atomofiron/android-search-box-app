package app.atomofiron.searchboxapp.screens.explorer

import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.common.util.insets.ViewGroupInsetsProxy
import app.atomofiron.common.util.insets.ViewInsetsController
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.FragmentExplorerBinding
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerAdapter
import app.atomofiron.searchboxapp.screens.explorer.fragment.HeaderViewOutputDelegate
import app.atomofiron.searchboxapp.screens.explorer.places.PlacesAdapter
import app.atomofiron.searchboxapp.screens.explorer.sheet.BottomSheetMenuWithTitle
import app.atomofiron.searchboxapp.screens.explorer.sheet.CreateDelegate
import app.atomofiron.searchboxapp.screens.explorer.sheet.RenameDelegate
import app.atomofiron.searchboxapp.screens.main.util.KeyCodeConsumer

class ExplorerFragment : Fragment(R.layout.fragment_explorer),
    BaseFragment<ExplorerFragment, ExplorerViewModel, ExplorerPresenter> by BaseFragmentImpl(),
    KeyCodeConsumer
{
    private lateinit var binding: FragmentExplorerBinding
    private lateinit var bottomItemMenu: BottomSheetMenuWithTitle
    private lateinit var renameDelegate: RenameDelegate
    private lateinit var createDelegate: CreateDelegate

    private val explorerAdapter = ExplorerAdapter()
    private val placesAdapter = PlacesAdapter()

    private lateinit var headerViewOutputDelegate: HeaderViewOutputDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, ExplorerViewModel::class, savedInstanceState)

        explorerAdapter.itemActionListener = presenter
        placesAdapter.itemActionListener = presenter

        renameDelegate = RenameDelegate(presenter)
        createDelegate = CreateDelegate(presenter)
        bottomItemMenu = BottomSheetMenuWithTitle(R.menu.item_options_explorer, requireContext(), presenter)

        headerViewOutputDelegate = HeaderViewOutputDelegate(explorerAdapter, presenter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentExplorerBinding.bind(view)

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = explorerAdapter

        binding.bottomBar.setOnMenuItemClickListener { id ->
            when (id) {
                R.id.menu_places -> binding.verticalDock.open()
                R.id.menu_search -> presenter.onSearchOptionSelected()
                R.id.menu_options -> presenter.onOptionsOptionSelected()
                R.id.menu_settings -> presenter.onSettingsOptionSelected()
            }
        }

        binding.verticalDock.run {
            onGravityChangeListener = presenter::onDockGravityChange
            recyclerView.adapter = placesAdapter
        }

        renameDelegate.bottomSheetView = binding.bottomSheet
        createDelegate.bottomSheetView = binding.bottomSheet
        bottomItemMenu.bottomSheetView = binding.bottomSheet

        explorerAdapter.setHeaderView(binding.explorerHeader)
        binding.explorerHeader.setOnItemActionListener(headerViewOutputDelegate)
        viewModel.onViewCollect()
        onApplyInsets(view)
    }

    override fun ExplorerViewModel.onViewCollect() {
        viewCollect(items, collector = explorerAdapter::setItems)
        viewCollect(itemComposition, collector = explorerAdapter::setComposition)
        viewCollect(current, collector = explorerAdapter::setCurrentDir)
        viewCollect(notifyUpdate, collector = explorerAdapter::setItem)
        viewCollect(notifyRemove, collector = explorerAdapter::removeItem)
        viewCollect(notifyInsert) { explorerAdapter.insertItem(it.first, it.second) }
        viewCollect(notifyUpdateRange, collector = explorerAdapter::notifyItems)
        viewCollect(notifyRemoveRange, collector = explorerAdapter::removeItems)
        viewCollect(notifyInsertRange) { explorerAdapter.insertItems(it.first, it.second) }
        viewCollect(permissionRequiredWarning, collector = ::showPermissionRequiredWarning)
        viewCollect(historyDrawerGravity) { binding.verticalDock.gravity = it }
        viewCollect(places, collector = placesAdapter::setItems)
        viewCollect(showOptions, collector = bottomItemMenu::show)
        viewCollect(showRename, collector = renameDelegate::show)
        viewCollect(showCreate, collector = createDelegate::show)
        viewCollect(scrollToCurrentDir, collector = explorerAdapter::scrollToCurrentDir)
    }

    override fun onApplyInsets(root: View) {
        ViewGroupInsetsProxy.set(root)
        ViewGroupInsetsProxy.set(binding.coordinator)
        ViewGroupInsetsProxy.set(binding.verticalDock)
        ViewInsetsController.bindPadding(binding.recyclerView, start = true, top = true, end = true, bottom = true)
        ViewInsetsController.bindPadding(binding.explorerHeader, start = true, top = true, end = true)
        ViewInsetsController.bindPadding(binding.bottomAppBar, start = true, bottom = true, end = true)
    }

    override fun onBack(): Boolean {
        val consumed = binding.verticalDock.run {
            when {
                isOpened -> {
                    close()
                    true
                }
                else -> binding.bottomSheet.hide()
            }
        }
        return consumed || super.onBack()
    }

    override fun onKeyDown(keyCode: Int): Boolean = when {
        binding.bottomSheet.isSheetShown -> false
        isHidden -> false
        keyCode == KeyEvent.KEYCODE_VOLUME_UP -> {
            presenter.onVolumeUp(explorerAdapter.isCurrentDirVisible())
            true
        }
        else -> false
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
}