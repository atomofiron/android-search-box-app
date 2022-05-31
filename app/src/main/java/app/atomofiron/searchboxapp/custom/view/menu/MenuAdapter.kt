package app.atomofiron.searchboxapp.custom.view.menu

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R

@SuppressLint("RestrictedApi")
class MenuAdapter(context: Context) : RecyclerView.Adapter<MenuHolder>() {
    val menu = MenuImpl(context)

    lateinit var menuListener: MenuListener

    init {
        menu.setMenuChangedListener(::onMenuChanged)
    }

    private fun onMenuChanged() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_curtain_menu, parent, false)
        return MenuHolder(view, menuListener)
    }

    override fun getItemCount(): Int = menu.size()

    override fun onBindViewHolder(holder: MenuHolder, position: Int) {
        holder.bind(menu.getItem(position))
    }
}