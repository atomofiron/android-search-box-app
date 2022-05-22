package app.atomofiron.searchboxapp.screens.explorer.sheet

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.bottom_sheet_menu.BottomSheetMenu
import app.atomofiron.searchboxapp.custom.view.bottom_sheet_menu.BottomSheetMenuListener
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerHolder

class BottomSheetMenuWithTitle(
    menuId: Int,
    context: Context,
    menuItemClickListener: BottomSheetMenuListener
) : BottomSheetMenu(R.layout.sheet_explorer_options, context, menuId, menuItemClickListener) {
    private val tvTitle: TextView get() = bottomSheetView.contentView.findViewById(R.id.explorer_menu_tv_title)
    private val exItem: View get() = bottomSheetView.contentView.findViewById(R.id.explorer_menu_item)
    private val exItemCheckBox: View get() = exItem.findViewById(R.id.item_explorer_cb)
    val tvDescription: TextView get() = bottomSheetView.contentView.findViewById(R.id.explorer_menu_tv_description)
    override val recyclerView: RecyclerView get() = bottomSheetView.contentView.findViewById(R.id.explorer_menu_rv)
    override var contentView: View? = null

    override fun show(items: List<Int>) = throw Exception()

    fun show(options: ExplorerItemOptions) {
        super.show(options.ids)
        when (options.items.size) {
            1 -> {
                tvTitle.isGone = true
                exItem.isVisible = true
                val holder = ExplorerHolder(exItem)
                holder.bind(options.items.first())
                holder.bindComposition(options.composition)
            }
            else -> {
                tvTitle.isVisible = true
                exItem.isGone = true
                var files = 0
                var dirs = 0
                options.items.forEach {
                    if (it.isDirectory) dirs++ else files++
                }
                val string = StringBuilder()
                if (dirs > 0) {
                    string.append(context.resources.getQuantityString(R.plurals.x_dirs, dirs, dirs))
                }
                if (dirs > 0 && files > 0) {
                    string.append(", ")
                }
                if (files > 0) {
                    string.append(context.resources.getQuantityString(R.plurals.x_files, files, files))
                }
                tvTitle.text = string.toString()
            }
        }
        disableClicks()
    }

    private fun disableClicks() {
        exItem.isFocusable = false
        exItem.isClickable = false
        exItem.background = null
        exItemCheckBox.isFocusable = false
        exItemCheckBox.isClickable = false
    }
}