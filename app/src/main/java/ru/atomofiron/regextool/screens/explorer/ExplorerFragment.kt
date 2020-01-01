package ru.atomofiron.regextool.screens.explorer

import android.os.Bundle
import android.view.View
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView {
            layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
            adapter = ExplorerAdapter()
        }

        bottomOptionMenu {
            setOnMenuItemClickListener { id ->
                when (id) {
                    R.id.menu_bookmark -> viewModel.onBookmarksOptionSelected()
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