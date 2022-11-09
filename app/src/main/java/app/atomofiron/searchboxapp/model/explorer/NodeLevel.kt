package app.atomofiron.searchboxapp.model.explorer

class NodeLevel(
    val parentPath: String,
    val children: MutableList<Node>,
) {
    val count: Int get() = children.size

    fun getOpenedIndex(): Int = children.indexOfFirst { it.isOpened }
    fun getOpened(): Node? = getOpenedIndex().takeIf { it >= 0 }?.let { children[it] }
}
