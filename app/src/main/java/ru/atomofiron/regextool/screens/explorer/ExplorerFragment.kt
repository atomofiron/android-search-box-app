package ru.atomofiron.regextool.screens.explorer

import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.base.BaseFragment
import ru.atomofiron.regextool.common.util.Knife
import ru.atomofiron.regextool.screens.explorer.adapter.ExplorerAdapter
import ru.atomofiron.regextool.view.custom.BottomOptionMenu
import ru.atomofiron.regextool.view.custom.VerticalDockView
import ru.atomofiron.regextool.view.custom.bottom_sheet_menu.BottomSheetView

class ExplorerFragment : BaseFragment<ExplorerViewModel>() {
    override val viewModelClass = ExplorerViewModel::class
    override val layoutId: Int = R.layout.fragment_explorer

    private val recyclerView = Knife<RecyclerView>(this, R.id.explorer_rv)
    private val bottomOptionMenu = Knife<BottomOptionMenu>(this, R.id.explorer_bom)
    private val bottomSheetView = Knife<BottomSheetView>(this, R.id.explorer_bsv)
    private val dockView = Knife<VerticalDockView>(this, R.id.explorer_dv)

    private val explorerAdapter = ExplorerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.files.observe(this, Observer(explorerAdapter::setItems))
        viewModel.notifyCurrent.observeData(this, explorerAdapter::setCurrentDir)
        viewModel.notifyUpdate.observeData(this, explorerAdapter::setItem)
        viewModel.notifyRemove.observeData(this, explorerAdapter::removeItem)
        viewModel.notifyInsert.observeData(this) { explorerAdapter.insertItem(it.first, it.second) }
        viewModel.notifyRemoveRange.observeData(this, explorerAdapter::removeItems)
        viewModel.notifyInsertRange.observeData(this) { explorerAdapter.insertItems(it.first, it.second) }
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
                    R.id.menu_tasks -> dockView { open() }
                    R.id.menu_search -> viewModel.onSearchOptionSelected()
                    R.id.menu_config -> bottomSheetView { show() }
                    R.id.menu_settings -> viewModel.onSettingsOptionSelected()
                }
            }
        }
        dockView {
            onGravityChangeListener = viewModel::onDockGravityChange
        }
    }

    override fun onSubscribeData(owner: LifecycleOwner) {
        viewModel.historyDrawerGravity.observe(owner, Observer { dockView { gravity = it } })
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
}