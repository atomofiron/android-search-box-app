package app.atomofiron.searchboxapp.model.explorer

import android.graphics.Bitmap
import java.util.Objects

data class NodeRoot(
    val type: NodeRootType,
    val item: Node,
    val thumbnail: Bitmap? = null,
    val isSelected: Boolean = false,
) {
    val stableId: Int = type.stableId

    sealed class NodeRootType {
        open val stableId: Int = Objects.hash(this::class)

        object Photos : NodeRootType()
        object Videos : NodeRootType()
        object Camera : NodeRootType()
        object Downloads : NodeRootType()
        object Bluetooth : NodeRootType()
        object Screenshots : NodeRootType()
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