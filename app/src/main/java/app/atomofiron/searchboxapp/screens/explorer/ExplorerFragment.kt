package app.atomofiron.searchboxapp.screens.explorer

import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.arch.fragment.BaseFragment
import app.atomofiron.common.util.Knife
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.BottomMenuBar
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView
import app.atomofiron.searchboxapp.custom.view.VerticalDockView
import app.atomofiron.searchboxapp.custom.view.bottom_sheet.BottomSheetView
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerAdapter
import app.atomofiron.searchboxapp.screens.explorer.fragment.HeaderViewOutputDelegate
import app.atomofiron.searchboxapp.screens.explorer.places.PlacesAdapter
import app.atomofiron.searchboxapp.screens.explorer.sheet.BottomSheetMenuWithTitle
import app.atomofiron.searchboxapp.screens.explorer.sheet.CreateDelegate
import app.atomofiron.searchboxapp.screens.explorer.sheet.RenameDelegate
import javax.inject.Inject

class ExplorerFragment : BaseFragment<ExplorerViewModel, ExplorerPresenter>() {
    override val viewModelClass = ExplorerViewModel::class
    override val layoutId: Int = R.layout.fragment_explorer

    private val recyclerView = Knife<RecyclerView>(this, R.id.explorer_rv)
    private val bottomMenuBar = Knife<BottomMenuBar>(this, R.id.explorer_bom)
    private val dockView = Knife<VerticalDockView>(this, R.id.explorer_dv)
    private val bottomSheetView = Knife<BottomSheetView>(this, R.id.explorer_bsv)
    private val headerView = Knife<ExplorerHeaderView>(this, R.id.explorer_ehv)
    private lateinit var bottomItemMenu: BottomSheetMenuWithTitle
    private lateinit var renameDelegate: RenameDelegate
    private lateinit var createDelegate: CreateDelegate

    private val explorerAdapter = ExplorerAdapter()
    private val placesAdapter = PlacesAdapter()

    private lateinit var headerViewOutputDelegate: HeaderViewOutputDelegate

    @Inject
    override lateinit var presenter: ExplorerPresenter

    override fun inject() = viewModel.inject(this)

    override fun onCreate() {
        explorerAdapter.itemActionListener = presenter
        placesAdapter.itemActionListener = presenter

        renameDelegate = RenameDelegate(presenter)
        createDelegate = CreateDelegate(presenter)
        bottomItemMenu = BottomSheetMenuWithTitle(R.menu.item_options_explorer, thisContext, presenter)

        headerViewOutputDelegate = HeaderViewOutputDelegate(explorerAdapter, presenter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView {
            layoutManager = LinearLayoutManager(context)
            adapter = explorerAdapter
        }

        bottomMenuBar {
            setOnMenuItemClickListener { id ->
                when (id) {
                    R.id.menu_places -> dockView { open() }
                    R.id.menu_search -> presenter.onSearchOptionSelected()
                    R.id.menu_options -> presenter.onOptionsOptionSelected()
                    R.id.menu_settings -> presenter.onSettingsOptionSelected()
                }
            }
        }
        dockView {
            onGravityChangeListener = presenter::onDockGravityChange

            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = placesAdapter
        }

        renameDelegate.bottomSheetView = bottomSheetView.view
        createDelegate.bottomSheetView = bottomSheetView.view
        bottomItemMenu.bottomSheetView = bottomSheetView.view

        headerView {
            explorerAdapter.setHeaderView(this)
            setOnItemActionListener(headerViewOutputDelegate)
        }
    }

    override fun onSubscribeData(owner: LifecycleOwner) {
        viewModel.items.observe(owner, Observer(explorerAdapter::setItems))
        viewModel.itemComposition.observe(owner, Observer(explorerAdapter::setComposition))
        viewModel.current.observe(owner, Observer(explorerAdapter::setCurrentDir))
        viewModel.notifyUpdate.observeData(owner, explorerAdapter::setItem)
        viewModel.notifyRemove.observeData(owner, explorerAdapter::removeItem)
        viewModel.notifyInsert.observeData(owner) { explorerAdapter.insertItem(it.first, it.second) }
        viewModel.notifyUpdateRange.observeData(owner, explorerAdapter::notifyItems)
        viewModel.notifyRemoveRange.observeData(owner, explorerAdapter::removeItems)
        viewModel.notifyInsertRange.observeData(owner) { explorerAdapter.insertItems(it.first, it.second) }
        viewModel.permissionRequiredWarning.observeEvent(owner, ::showPermissionRequiredWarning)
        viewModel.historyDrawerGravity.observe(owner, Observer { dockView { gravity = it } })
        viewModel.places.observe(owner, Observer(placesAdapter::setItems))
        viewModel.showOptions.observeData(owner, bottomItemMenu::show)
        viewModel.showRename.observeData(owner, renameDelegate::show)
        viewModel.showCreate.observeData(owner, createDelegate::show)
        viewModel.scrollToCurrentDir.observeEvent(owner, explorerAdapter::scrollToCurrentDir)
    }

    override fun onBack(): Boolean {
        val consumed = dockView(default = false) {
            when {
                isOpened -> {
                    close()
                    true
                }
                bottomSheetView(default = false) { hide() } -> true
                else -> false
            }
        }
        return consumed || super.onBack()
    }

    fun onKeyDown(keyCode: Int): Boolean = when {
        bottomSheetView(default = false) { isSheetShown } -> false
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

    private fun showPermissionRequiredWarning() {
        Snackbar.make(thisView, R.string.access_to_storage_forbidden, Snackbar.LENGTH_LONG)
                .setAnchorView(anchorView)
                .setAction(R.string.allow) { presenter.onAllowStorageClick() }
                .show()
    }
}