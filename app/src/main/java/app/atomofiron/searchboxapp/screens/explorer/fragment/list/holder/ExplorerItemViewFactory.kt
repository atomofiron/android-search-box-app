package app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder

import android.view.View
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.ItemExplorerBinding
import app.atomofiron.searchboxapp.databinding.ItemExplorerSeparatorBinding
import app.atomofiron.searchboxapp.model.explorer.Node

enum class ExplorerItemViewFactory(val layoutId: Int) {
    NodeItem(R.layout.item_explorer) {
        override fun createHolder(itemView: View, rootAliases: Map<Int, String>): ExplorerHolder {
            return ExplorerHolder(itemView, rootAliases)
        }
    },
    OpenedNodeItem(R.layout.item_explorer) {
        override fun createHolder(itemView: View, rootAliases: Map<Int, String>): ExplorerHolder {
            ItemExplorerBinding.bind(itemView).makeOpened()
            return ExplorerHolder(itemView, rootAliases)
        }
    },
    CurrentOpenedNodeItem(R.layout.item_explorer) {
        override fun createHolder(itemView: View, rootAliases: Map<Int, String>): ExplorerHolder {
            ItemExplorerBinding.bind(itemView).makeOpenedCurrent()
            return ExplorerHolder(itemView, rootAliases)
        }
    },
    SeparatorNodeItem(R.layout.item_explorer_separator) {
        override fun createHolder(itemView: View, rootAliases: Map<Int, String>): ExplorerSeparatorHolder {
            ItemExplorerSeparatorBinding.bind(itemView).makeSeparator()
            return ExplorerSeparatorHolder(itemView, rootAliases)
        }
    };

    abstract fun createHolder(itemView: View, rootAliases: Map<Int, String>): GeneralHolder<Node>
}