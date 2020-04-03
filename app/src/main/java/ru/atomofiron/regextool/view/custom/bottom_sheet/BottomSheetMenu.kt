package ru.atomofiron.regextool.view.custom.bottom_sheet

import android.content.Context
import android.view.MenuInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BottomSheetMenu(context: Context) : BottomSheetDelegate() {
    private val rvMenu: RecyclerView = RecyclerView(context)
    override var contentView: View? = rvMenu

    var menuItemClickListener: (id: Int) -> Unit = { }

    private fun onMenuItemClick(id: Int) = menuItemClickListener.invoke(id)

    fun inflateMenu(context: Context, menuId: Int) {
        rvMenu.layoutManager = LinearLayoutManager(context)
        rvMenu.adapter = BottomSheetViewAdapter(context).apply {
            MenuInflater(context).inflate(menuId, menu)
            menuItemClickListener = ::onMenuItemClick
        }
    }
}