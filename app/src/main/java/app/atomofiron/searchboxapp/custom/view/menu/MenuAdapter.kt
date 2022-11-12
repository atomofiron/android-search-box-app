package app.atomofiron.searchboxapp.custom.view.menu

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.ColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R

@SuppressLint("RestrictedApi")
class MenuAdapter(context: Context) : RecyclerView.Adapter<MenuHolder>() {
    val menu = MenuImpl(context)

    lateinit var menuListener: MenuListener
    private var dangerousItemId = 0

    init {
        menu.setMenuChangedListener(::onMenuChanged)
    }

    fun markAsDangerous(itemId: Int) {
        dangerousItemId = itemId
    }

    private fun onMenuChanged() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_curtain_menu, parent, false)
        view.background.mutate()
        return MenuHolder(view, menuListener)
    }

    override fun getItemCount(): Int = menu.size()

    override fun onBindViewHolder(holder: MenuHolder, position: Int) {
        val item = menu.getItem(position)
        holder.bind(item)
        val backgroundId = when (item.itemId) {
            dangerousItemId -> R.drawable.item_menu_dangerous
            else -> R.drawable.item_menu
        }
        holder.itemView.setBackgroundResource(backgroundId)
        holder.icon.colorFilter = when (item.itemId) {
            dangerousItemId -> {
                val color = ContextCompat.getColor(holder.itemView.context, R.color.danger_red)
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_IN)
            }
            else -> null
        }
    }
}