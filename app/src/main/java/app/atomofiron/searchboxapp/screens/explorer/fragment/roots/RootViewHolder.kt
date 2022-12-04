package app.atomofiron.searchboxapp.screens.explorer.fragment.roots

import android.graphics.drawable.ColorDrawable
import android.view.View
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.ItemExplorerCardBinding
import app.atomofiron.searchboxapp.getColorByAttr
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.model.explorer.NodeRoot.NodeRootType

class RootViewHolder(itemView: View) : GeneralHolder<NodeRoot>(itemView) {

    private val binding = ItemExplorerCardBinding.bind(itemView)

    init {
        val clr = itemView.context.getColorByAttr(R.attr.colorOutline)
        binding.cartThumbnail.foreground = ColorDrawable(clr)
        binding.cartThumbnail.clipToOutline = true
        binding.cartThumbnail.setBackgroundResource(R.drawable.clip_card_thumbnail)
    }

    override fun onBind(item: NodeRoot, position: Int) {
        binding.cartTitle.text = item.getTitle()
    }

    private fun NodeRoot.getTitle(): String {
        return when (type) {
            is NodeRootType.Photos -> context.getString(R.string.root_photos)
            is NodeRootType.Videos -> context.getString(R.string.root_videos)
            is NodeRootType.Camera -> context.getString(R.string.root_camera)
            is NodeRootType.Downloads -> context.getString(R.string.root_downloads)
            is NodeRootType.Bluetooth -> context.getString(R.string.root_bluetooth)
            is NodeRootType.Screenshots -> context.getString(R.string.root_screenshots)
            is NodeRootType.InternalStorage -> context.getString(R.string.internal_storage)
            is NodeRootType.Favorite -> ""
        }
    }
}