package app.atomofiron.searchboxapp.model.explorer

import android.graphics.Bitmap
import java.util.Objects

data class NodeRoot(
    val type: NodeRootType,
    val thumbnail: Bitmap? = null,
) {
    sealed class NodeRootType {
        object Photos : NodeRootType()
        object Videos : NodeRootType()
        object Camera : NodeRootType()
        object Downloads : NodeRootType()
        object Bluetooth : NodeRootType()
        object Screenshots : NodeRootType()
        object InternalStorage : NodeRootType()
        class Favorite(val item: Node) : NodeRootType()
    }

    override fun equals(other: Any?): Boolean = when {
        other !is NodeRoot -> false
        other.type != type -> false
        other.thumbnail !== thumbnail -> false
        else -> true
    }

    override fun hashCode(): Int = Objects.hash(type, thumbnail)
}