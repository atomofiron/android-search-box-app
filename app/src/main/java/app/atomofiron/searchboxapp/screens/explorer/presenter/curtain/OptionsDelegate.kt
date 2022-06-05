package app.atomofiron.searchboxapp.screens.explorer.presenter.curtain

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.databinding.CurtainExplorerOptionsBinding
import app.atomofiron.searchboxapp.databinding.ItemExplorerBinding
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerHolder
import lib.atomofiron.android_window_insets_compat.ViewInsetsController

class OptionsDelegate(
    private val menuId: Int,
    private val output: MenuListener,
) {

    fun getView(options: ExplorerItemOptions, inflater: LayoutInflater, container: ViewGroup): View {
        val binding = CurtainExplorerOptionsBinding.inflate(inflater, container, false)
        binding.menuView.run {
            inflateMenu(menuId)
            setMenuListener(output)
            markAsDangerous(R.id.menu_remove)
        }
        binding.init(container.context, options)
        ViewInsetsController.bindPadding(binding.root, top = true, withProxy = true)
        ViewInsetsController.bindPadding(binding.menuView, bottom = true)
        return binding.root
    }

    fun CurtainExplorerOptionsBinding.init(context: Context, options: ExplorerItemOptions) {
        when (options.items.size) {
            1 -> {
                explorerMenuTvTitle.isGone = true
                explorerMenuItem.root.isVisible = true
                val holder = ExplorerHolder(explorerMenuItem.root)
                holder.bind(options.items.first())
                holder.bindComposition(options.composition)
            }
            else -> {
                explorerMenuTvTitle.isVisible = true
                explorerMenuItem.root.isGone = true
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
                explorerMenuTvTitle.text = string.toString()
            }
        }
        explorerMenuItem.disableClicks()
    }

    private fun ItemExplorerBinding.disableClicks() {
        itemExplorer.isFocusable = false
        itemExplorer.isClickable = false
        itemExplorer.isLongClickable = false
        itemExplorer.setBackgroundColor(ContextCompat.getColor(root.context, R.color.item_explorer_background))
        itemExplorerCb.isEnabled = false
    }
}