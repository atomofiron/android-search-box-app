package ru.atomofiron.regextool.custom.view.bottom_sheet_menu

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.menu.MenuImpl

class BottomSheetMenuAdapter(context: Context) : RecyclerView.Adapter<BottomSheetMenuHolder>() {
    val menu = MenuImpl(context)

    lateinit var menuItemClickListener: BottomSheetMenuListener

    init {
        menu.setMenuChangedListener(::onMenuChanged)
    }

    private fun onMenuChanged() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomSheetMenuHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_bottom_sheet_menu, parent, false)
        return BottomSheetMenuHolder(view, menuItemClickListener)
    }

    @SuppressLint("RestrictedApi")
    override fun getItemCount(): Int = menu.size()

    @SuppressLint("RestrictedApi")
    override fun onBindViewHolder(holder: BottomSheetMenuHolder, position: Int) {
        holder.bind(menu.getItem(position))
    }
}