package app.atomofiron.searchboxapp.model.finder

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch
import app.atomofiron.searchboxapp.utils.Const
import java.util.*

sealed class SearchResult(
    val count: Int,
    val countMax: Int = -100,
) {

    val isEmpty: Boolean = count == 0

    abstract fun getCounters(): IntArray

    class TextSearchResult(
        count: Int,
        /** line index -> matches byteOffset+length */
        val matchesMap: Map<Int, List<TextLineMatch>>,
        val indexes: List<Int>,
    ) : SearchResult(count) {

        constructor() : this(0, mapOf(), listOf())

        override fun getCounters(): IntArray = intArrayOf(count)
    }

    class FinderResult private constructor(
        val forContent: Boolean,
        count: Int = 0,
        val tuples: List<ItemCounter> = listOf(),
        countMax: Int = 0,
    ) : SearchResult(if (forContent) count else tuples.size, countMax) {
        companion object {
            fun forContent() = FinderResult(forContent = true)
            fun forNames() = FinderResult(forContent = false)
        }

        override fun getCounters(): IntArray = when {
            forContent -> intArrayOf(count, tuples.size, countMax)
            else -> intArrayOf(tuples.size, countMax)
        }

        fun toMarkdown(): String {
            val data = StringBuilder()
            for (item in tuples) {
                val name = if (item.item.isDirectory) item.item.name + Const.SLASH else item.item.name
                val line = String.format("[%s](%s)\n", name, item.item.path.replace(" ", "\\ "))
                data.append(line)
            }
            return data.toString()
        }

        fun removeItem(item: Node): SearchResult {
            val index = tuples.indexOfFirst { it.item.uniqueId == item.uniqueId }
            if (index < 0) return this
            val items = tuples.toMutableList()
            val removed = items.removeAt(index)
            val count = count - removed.count
            return FinderResult(forContent, count, items, countMax.dec())
        }

        fun add(itemCounter: ItemCounter, dCountMax: Int): FinderResult {
            val items = tuples.toMutableList()
            val count = count + itemCounter.count
            items.add(itemCounter)
            return FinderResult(forContent, count, items, countMax + dCountMax)
        }
    }

    override fun hashCode(): Int = Objects.hash(this::class, count, countMax)

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other !is SearchResult -> false
        other::class != this::class -> false
        other.count != count -> false
        other.countMax != countMax -> false
        else -> false
    }
}

class ItemCounter(
    val item: Node,
    val count: Int = 1,
) {
    val path = item.path
    val isDirectory: Boolean = item.isDirectory

    val name: String = item.name
    val isCached: Boolean = item.isCached

    var isChecked: Boolean = false
    val isDeleting: Boolean = item.state.isDeleting

    fun updateCache(useSu: Boolean) = Unit//xFile.updateCache(useSu, completely = true)

    override fun toString(): String = "ItemWithCounter{count=$count,path=${item.path}}"
}
