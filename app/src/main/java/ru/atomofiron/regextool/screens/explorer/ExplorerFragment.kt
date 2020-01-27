package ru.atomofiron.regextool.screens.explorer

import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.base.BaseFragment
import ru.atomofiron.regextool.common.util.Knife
import ru.atomofiron.regextool.screens.explorer.adapter.ExplorerAdapter
import ru.atomofiron.regextool.view.custom.BottomOptionMenu
import ru.atomofiron.regextool.view.custom.bottom_sheet_menu.BottomSheetView

class ExplorerFragment : BaseFragment<ExplorerViewModel>() {
    override val viewModelClass = ExplorerViewModel::class
    override val layoutId: Int = R.layout.fragment_explorer

    private val recyclerView = Knife<RecyclerView>(this, R.id.explorer_rv)
    private val bottomOptionMenu = Knife<BottomOptionMenu>(this, R.id.explorer_bom)
    private val bottomSheetView = Knife<BottomSheetView>(this, R.id.explorer_bsv)
    private val drawer = Knife<DrawerLayout>(this, R.id.explorer_dv)

    private val explorerAdapter = ExplorerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.files.observe(this, Observer(explorerAdapter::setItems))
        viewModel.notifyCurrent.observe(this, explorerAdapter::setCurrentDir)
        viewModel.notifyUpdate.observe(this, explorerAdapter::setItem)
        viewModel.notifyRemove.observe(this, explorerAdapter::removeItem)
        viewModel.notifyInsert.observe(this) { explorerAdapter.insertItem(it.first, it.second) }
        viewModel.notifyRemoveRange.observe(this, explorerAdapter::removeItems)
        viewModel.notifyInsertRange.observe(this) { explorerAdapter.insertItems(it.first, it.second) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView {
            layoutManager = LinearLayoutManager(context)
            adapter = explorerAdapter
            explorerAdapter.setOnItemActionListener(viewModel)
        }

        bottomOptionMenu {
            setOnMenuItemClickListener { id ->
                when (id) {
                    R.id.menu_bookmark -> drawer.view.openDrawer(GravityCompat.START, true)
                    R.id.menu_search -> viewModel.onSearchOptionSelected()
                    R.id.menu_config -> bottomSheetView.view.show()
                    R.id.menu_settings -> viewModel.onSettingsOptionSelected()
                }
            }
        }
    }

    override fun onBack(): Boolean {
        return if (bottomSheetView.view.isSheetShown) {
            bottomSheetView.view.hide()
            true
        } else {
            false
        }
    }
}