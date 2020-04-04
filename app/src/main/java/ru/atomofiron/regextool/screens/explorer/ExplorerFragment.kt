package ru.atomofiron.regextool.screens.explorer

import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.base.BaseFragment
import app.atomofiron.common.util.Knife
import com.google.android.material.snackbar.Snackbar
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.screens.explorer.adapter.ExplorerAdapter
import ru.atomofiron.regextool.screens.explorer.options.BottomSheetMenuWithTitle
import ru.atomofiron.regextool.screens.explorer.options.ExplorerItemOptions
import ru.atomofiron.regextool.screens.explorer.places.PlacesAdapter
import ru.atomofiron.regextool.view.custom.BottomMenuBar
import ru.atomofiron.regextool.view.custom.VerticalDockView
import ru.atomofiron.regextool.view.custom.bottom_sheet.BottomSheetView

class ExplorerFragment : BaseFragment<ExplorerViewModel>() {
    override val viewModelClass = ExplorerViewModel::class
    override val layoutId: Int = R.layout.fragment_explorer

    private val recyclerView = Knife<RecyclerView>(this, R.id.explorer_rv)
    private val bottomMenuBar = Knife<BottomMenuBar>(this, R.id.explorer_bom)
    private val bottomSheetView = Knife<BottomSheetView>(this, R.id.explorer_bsv)
    private lateinit var bottomItemMenu: BottomSheetMenuWithTitle
    private val dockView = Knife<VerticalDockView>(this, R.id.explorer_dv)

    private val explorerAdapter = ExplorerAdapter()
    private val placesAdapter = PlacesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        explorerAdapter.itemActionListener = viewModel
        placesAdapter.itemActionListener = viewModel
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
                    R.id.menu_search -> viewModel.onSearchOptionSelected()
                    R.id.menu_options -> viewModel.options?.let{ bottomItemMenu.show(it) }
                    R.id.menu_settings -> viewModel.onSettingsOptionSelected()
                }
            }
        }
        dockView {
            onGravityChangeListener = viewModel::onDockGravityChange

            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = placesAdapter
        }

        bottomItemMenu = BottomSheetMenuWithTitle(thisContext, viewModel)
        bottomItemMenu.bottomSheetView = bottomSheetView.view
    }

    override fun onSubscribeData(owner: LifecycleOwner) {
        viewModel.items.observe(owner, Observer(explorerAdapter::setItems))
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
        viewModel.showOptions.observeData(owner, ::showOptions)
        viewModel.itemComposition.observe(owner, Observer(explorerAdapter::setComposition))
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

    private fun showPermissionRequiredWarning() {
        Snackbar.make(thisView, R.string.access_to_storage_forbidden, Snackbar.LENGTH_LONG)
                .setAnchorView(anchorView)
                .setAction(R.string.allow) { viewModel.onAllowStorageClick() }
                .show()
    }

    private fun showOptions(options: ExplorerItemOptions) = bottomItemMenu.show(options)
}