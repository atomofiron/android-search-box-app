package app.atomofiron.searchboxapp.model.explorer

import android.graphics.drawable.Drawable
import java.util.Objects

data class NodeRoot(
    val type: NodeRootType,
    val item: Node,
    val thumbnail: Drawable? = null,
    val isSelected: Boolean = false,
) {
    val stableId: Int = type.stableId
    val isMedia: Boolean = when (type) {
        is NodeRootType.Photos,
        is NodeRootType.Videos,
        is NodeRootType.Camera,
        is NodeRootType.Screenshots -> true
        else -> false
    }

    sealed class NodeRootType {
        open val stableId: Int = Objects.hash(this::class)

        object Photos : NodeRootType()
        object Videos : NodeRootType()
        object Camera : NodeRootType()
        object Screenshots : NodeRootType()
        object Downloads : NodeRootType()
        object Bluetooth : NodeRootType()
        data class InternalStorage(
            val used: Long = 0,
            val free: Long = 0,
        ) : NodeRootType()
        object Favorite : NodeRootType()
    }

    override fun equals(other: Any?): Boolean = when {
        other !is NodeRoot -> false
        other.type != type -> false
        other.thumbnail !== thumbnail -> false
        else -> true
    }

    override fun hashCode(): Int = Objects.hash(type, thumbnail)
}