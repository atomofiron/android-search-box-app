package app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView.Companion.makeOpened
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView.Companion.makeOpenedCurrent
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView.Companion.makeSeparator
import app.atomofiron.searchboxapp.databinding.ItemExplorerBinding
import app.atomofiron.searchboxapp.databinding.ItemExplorerSeparatorBinding
import app.atomofiron.searchboxapp.model.explorer.Node

enum class ExplorerItemViewFactory {
    NodeItem {
        override fun createHolder(parent: ViewGroup, rootAliases: Map<Int, String>): GeneralHolder<Node> {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_explorer, parent, false)
            return ExplorerHolder(view, rootAliases)
        }
    },
    OpenedNodeItem {
        override fun createHolder(parent: ViewGroup, rootAliases: Map<Int, String>): GeneralHolder<Node> {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_explorer, parent, false)
            ItemExplorerBinding.bind(view).makeOpened()
            return ExplorerHolder(view, rootAliases)
        }
    },
    CurrentOpenedNodeItem {
        override fun createHolder(parent: ViewGroup, rootAliases: Map<Int, String>): GeneralHolder<Node> {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_explorer, parent, false)
            ItemExplorerBinding.bind(view).makeOpenedCurrent()
            return ExplorerHolder(view, rootAliases)
        }
    },
    SeparatorNodeItem {
        override fun createHolder(parent: ViewGroup, rootAliases: Map<Int, String>): GeneralHolder<Node> {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_explorer_separator, parent, false)
            ItemExplorerSeparatorBinding.bind(view).makeSeparator()
            return ExplorerSeparatorHolder(view, rootAliases)
        }
    };

    abstract fun createHolder(parent: ViewGroup, rootAliases: Map<Int, String>): GeneralHolder<Node>
}