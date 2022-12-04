package app.atomofiron.searchboxapp.screens.explorer.fragment.list

import android.view.View
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.ItemExplorerSeparatorBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.Node.Companion.toUniqueId
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.getExternalStorageDirectory
import app.atomofiron.searchboxapp.utils.Tool.endingDot

class ExplorerSeparatorHolder(itemView: View) : GeneralHolder<Node>(itemView) {

    private val binding = ItemExplorerSeparatorBinding.bind(itemView)
    private var clickListener: ((Node) -> Unit)? = null
    private val rootsAliases = HashMap<Int, Int>()

    init {
        itemView.setOnClickListener {
            clickListener?.invoke(item)
        }
        itemView.context.getExternalStorageDirectory()?.let {
            rootsAliases[it.endingDot().toUniqueId()] = R.string.internal_storage
        }
        rootsAliases[Const.ROOT.endingDot().toUniqueId()] = R.string.root
    }

    override fun onBind(item: Node, position: Int) {
        val aliasId = rootsAliases[item.uniqueId]
        binding.itemExplorerTvTitle.text = when (aliasId) {
            null -> item.name
            else -> itemView.context.getString(aliasId)
        }
    }

    fun setOnClickListener(listener: (Node) -> Unit) {
        clickListener = listener
    }
}