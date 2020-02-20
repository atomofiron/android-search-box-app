package ru.atomofiron.regextool.screens.finder

import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.base.BaseFragment
import ru.atomofiron.regextool.common.util.DrawerStateListenerImpl
import ru.atomofiron.regextool.common.util.Knife
import ru.atomofiron.regextool.screens.finder.adapter.FinderAdapter
import ru.atomofiron.regextool.screens.finder.adapter.OnFinderActionListener
import ru.atomofiron.regextool.screens.finder.adapter.item.FinderItem.FieldItem
import ru.atomofiron.regextool.screens.finder.adapter.item.FinderItem.SomeItem
import ru.atomofiron.regextool.screens.finder.history.adapter.HistoryAdapter
import ru.atomofiron.regextool.view.custom.BottomOptionMenu
import kotlin.reflect.KClass

class FinderFragment : BaseFragment<FinderViewModel>() {
    override val viewModelClass: KClass<FinderViewModel> = FinderViewModel::class
    override val layoutId: Int = R.layout.fragment_finder

    private val recyclerView = Knife<RecyclerView>(this, R.id.finder_rv)
    private val viewHistory = Knife<RecyclerView>(this, R.id.finder_rv_history)
    private val bottomOptionMenu = Knife<BottomOptionMenu>(this, R.id.finder_bom)
    private val drawer = Knife<DrawerLayout>(this, R.id.finder_dl)

    private val drawerStateListener = DrawerStateListenerImpl()

    private val adapter: HistoryAdapter = HistoryAdapter(object : HistoryAdapter.OnItemClickListener {
        override fun onItemClick(node: String?) {
        }
    })
    private val onFinderActionListener: OnFinderActionListener
            by lazy { FinderAdapterDelegate(adapter, viewModel) }

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

        recyclerView {
            val linearLayoutManager = LinearLayoutManager(context!!)
            layoutManager = linearLayoutManager
            linearLayoutManager.reverseLayout = true
            val mainAdapter = FinderAdapter(onFinderActionListener)
            mainAdapter.setItems(items)
            adapter = mainAdapter
        }

        bottomOptionMenu.view.setOnMenuItemClickListener { id ->
            when (id) {
                R.id.menu_history -> drawer.view.openDrawer(GravityCompat.START, true)
                R.id.menu_explorer -> viewModel.onExplorerOptionSelected()
                R.id.menu_config -> viewModel.onConfigOptionSelected()
                R.id.menu_settings -> viewModel.onSettingsOptionSelected()
            }
        }

        viewHistory.view.adapter = adapter

        drawer.view.addDrawerListener(drawerStateListener)
    }

    override fun onBack(): Boolean {
        val consumed = drawer(default = false) {
            val opened = drawerStateListener.isOpened
            if (opened) {
                closeDrawer(GravityCompat.START)
            }
            opened
        }
        return consumed || super.onBack()
    }
}