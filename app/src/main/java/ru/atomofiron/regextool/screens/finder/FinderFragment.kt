package ru.atomofiron.regextool.screens.finder

import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.base.BaseFragment
import ru.atomofiron.regextool.common.util.Knife
import ru.atomofiron.regextool.screens.finder.adapter.FinderAdapter
import ru.atomofiron.regextool.screens.finder.adapter.OnFinderActionListener
import ru.atomofiron.regextool.screens.finder.adapter.item.FinderItem.FieldItem
import ru.atomofiron.regextool.screens.finder.adapter.item.FinderItem.SomeItem
import ru.atomofiron.regextool.screens.finder.history.adapter.HistoryAdapter
import ru.atomofiron.regextool.screens.finder.model.FinderState
import ru.atomofiron.regextool.view.custom.BottomOptionMenu
import ru.atomofiron.regextool.view.custom.VerticalDockView
import kotlin.reflect.KClass

class FinderFragment : BaseFragment<FinderViewModel>() {
    override val viewModelClass: KClass<FinderViewModel> = FinderViewModel::class
    override val layoutId: Int = R.layout.fragment_finder

    private val rvContent = Knife<RecyclerView>(this, R.id.finder_rv)
    private val bottomOptionMenu = Knife<BottomOptionMenu>(this, R.id.finder_bom)
    private val dockView = Knife<VerticalDockView>(this, R.id.finder_dv)

    private val historyAdapter: HistoryAdapter = HistoryAdapter(object : HistoryAdapter.OnItemClickListener {
        override fun onItemClick(node: String?) {
        }
    })
    private val onFinderActionListener: OnFinderActionListener
            by lazy { FinderAdapterDelegate(historyAdapter, viewModel) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val items = listOf(
                FieldItem(R.layout.layout_field_find, replace = false),
                SomeItem(R.layout.layout_characters),
                SomeItem(R.layout.layout_config),
                SomeItem(R.layout.layout_test),
                SomeItem(R.layout.layout_progress),
                SomeItem(R.layout.item_finder_file),
                SomeItem(R.layout.item_finder_file),
                SomeItem(R.layout.item_finder_file),
                SomeItem(R.layout.item_finder_file),
                SomeItem(R.layout.item_finder_file),
                SomeItem(R.layout.item_finder_file),
                SomeItem(R.layout.item_finder_file),
                SomeItem(R.layout.item_finder_file),
                SomeItem(R.layout.item_finder_file),
                SomeItem(R.layout.item_finder_file),
                SomeItem(R.layout.item_finder_file),
                SomeItem(R.layout.item_finder_file),
                SomeItem(R.layout.item_finder_file),
                SomeItem(R.layout.item_finder_file),
                SomeItem(R.layout.item_finder_file),
                SomeItem(R.layout.item_finder_file)
        )

        rvContent {
            val linearLayoutManager = LinearLayoutManager(context!!)
            layoutManager = linearLayoutManager
            linearLayoutManager.reverseLayout = true
            val mainAdapter = FinderAdapter(onFinderActionListener)
            mainAdapter.setItems(items)
            adapter = mainAdapter
        }

        bottomOptionMenu.view.setOnMenuItemClickListener { id ->
            when (id) {
                R.id.menu_history -> dockView { open() }
                R.id.menu_explorer -> viewModel.onExplorerOptionSelected()
                R.id.menu_config -> viewModel.onConfigOptionSelected()
                R.id.menu_settings -> viewModel.onSettingsOptionSelected()
            }
        }

        dockView {
            recyclerView.adapter = historyAdapter
            onGravityChangeListener = viewModel::onDockGravityChange
        }
    }

    override fun onSubscribeData(owner: LifecycleOwner) {
        viewModel.historyDrawerGravity.observe(owner, Observer { dockView { gravity = it } })
        viewModel.reloadHistory.observeEvent(owner, historyAdapter::reload)
        viewModel.state.observe(owner, Observer(::onStateChange))
    }

    override fun onBack(): Boolean {
        val consumed = dockView(default = false) {
            isOpened.apply {
                close()
            }
        }
        return consumed || super.onBack()
    }

    private fun onStateChange(state: FinderState) {

    }
}