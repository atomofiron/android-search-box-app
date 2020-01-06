package ru.atomofiron.regextool.screens.finder

import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.base.BaseFragment
import ru.atomofiron.regextool.common.util.Knife
import ru.atomofiron.regextool.screens.finder.adapter.FinderAdapter
import ru.atomofiron.regextool.screens.finder.adapter.FinderItem
import ru.atomofiron.regextool.view.custom.BottomOptionMenu
import kotlin.reflect.KClass

class FinderFragment : BaseFragment<FinderViewModel>() {
    override val viewModelClass: KClass<FinderViewModel> = FinderViewModel::class
    override val layoutId: Int = R.layout.fragment_finder

    private val recyclerView = Knife<RecyclerView>(this, R.id.finder_rv)
    private val bottomOptionMenu = Knife<BottomOptionMenu>(this, R.id.finder_bom)
    private val drawer = Knife<DrawerLayout>(this, R.id.finder_dl)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val items = listOf(
                FinderItem(R.layout.layout_field_find),
                FinderItem(R.layout.layout_characters),
                FinderItem(R.layout.layout_config),
                FinderItem(R.layout.layout_test),
                FinderItem(R.layout.layout_progress),
                FinderItem(R.layout.item_finder_file),
                FinderItem(R.layout.item_finder_file),
                FinderItem(R.layout.item_finder_file),
                FinderItem(R.layout.item_finder_file),
                FinderItem(R.layout.item_finder_file),
                FinderItem(R.layout.item_finder_file),
                FinderItem(R.layout.item_finder_file)
        )

        recyclerView {
            val linearLayoutManager = LinearLayoutManager(context!!)
            layoutManager = linearLayoutManager
            linearLayoutManager.reverseLayout = true
            val mainAdapter = FinderAdapter()
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
    }
}