package app.atomofiron.searchboxapp.screens.explorer.fragment.roots

import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.ItemExplorerCardBinding
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.model.explorer.NodeRoot.NodeRootType

class RootViewHolder(itemView: View) : GeneralHolder<NodeRoot>(itemView) {

    private val binding = ItemExplorerCardBinding.bind(itemView)

    init {
        binding.cartThumbnail.clipToOutline = true
    }

    override fun onBind(item: NodeRoot, position: Int) {
        binding.cartTitle.text = item.getTitle()
        when (item.thumbnail) {
            null -> binding.cartThumbnail.setImageDrawable(item.getIcon())
            else -> binding.cartThumbnail.setImageBitmap(item.thumbnail)
        }
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
            is NodeRootType.Favorite -> type.item.name
        }
    }

    private fun NodeRoot.getIcon(): Drawable? {
        val resId = when (type) {
            is NodeRootType.Photos -> R.drawable.ic_thumbnail_camera
            is NodeRootType.Videos -> R.drawable.ic_thumbnail_videocam
            is NodeRootType.Camera -> R.drawable.ic_thumbnail_camera
            is NodeRootType.Downloads -> R.drawable.ic_thumbnail_download
            is NodeRootType.Bluetooth -> R.drawable.ic_thumbnail_bluetooth
            is NodeRootType.Screenshots -> R.drawable.ic_thumbnail_screenshot
            is NodeRootType.InternalStorage -> R.drawable.ic_thumbnail_memory
            is NodeRootType.Favorite -> R.drawable.ic_thumbnail_favorite
        }
        return ContextCompat.getDrawable(context, resId)
    }
}