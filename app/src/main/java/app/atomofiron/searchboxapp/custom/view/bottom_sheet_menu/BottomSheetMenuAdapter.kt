package app.atomofiron.searchboxapp.custom.view.bottom_sheet_menu

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuImpl

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

    override fun getItemCount(): Int = menu.size()

    override fun onBindViewHolder(holder: BottomSheetMenuHolder, position: Int) {
        holder.bind(menu.getItem(position))
    }
}