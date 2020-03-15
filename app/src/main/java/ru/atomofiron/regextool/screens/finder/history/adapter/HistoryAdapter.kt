package ru.atomofiron.regextool.screens.finder.history.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.screens.finder.history.dao.HistoryDao
import ru.atomofiron.regextool.screens.finder.history.dao.HistoryDatabase
import ru.atomofiron.regextool.screens.finder.history.dao.ItemHistory

class HistoryAdapter(
        private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<HistoryHolder>(), HistoryHolder.OnItemActionListener {
    companion object {
        private const val DB_NAME = "history"
        private const val UNDEFINED = -1
        private const val FIRST = 0
    }

    private lateinit var db: HistoryDatabase
    private lateinit var dao: HistoryDao
    private lateinit var items: ArrayList<ItemHistory>

    init {
        setHasStableIds(true)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        recyclerView.layoutManager ?: { recyclerView.layoutManager = LinearLayoutManager(recyclerView.context) }()

        db = Room.databaseBuilder(recyclerView.context, HistoryDatabase::class.java, DB_NAME)
                .allowMainThreadQueries()
                .build()
        dao = db.historyDao()
        reload()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

        db.close()
    }

    fun add(string: String) {
        if (string.isEmpty()) {
            return
        }
        var index = items.indexOfFirst { !it.pinned }
        if (index == UNDEFINED) {
            index = items.size
        }
        val item = ItemHistory()
        item.title = string
        items.add(index, item)
        dao.insert(item)
        notifyItemInserted(index)
    }

    fun reload() {
        items = ArrayList(dao.all)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_history, parent, false)
        return HistoryHolder(view, this)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long = items[position].id

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        val item = items[position]
        holder.onBind(item.title, item.pinned)
    }

    override fun onItemClick(position: Int) {
        val item = items[position]
        onItemClickListener.onItemClick(item.title!!)
    }

    override fun onItemPin(position: Int) {
        val item = items[position]
        val nextPosition = if (item.pinned) items.indexOfLast { it.pinned } else FIRST
        item.pinned = !item.pinned
        dao.delete(item)
        item.id = 0L
        item.id = dao.insert(item)
        notifyItemChanged(position)

        items.removeAt(position)
        items.add(nextPosition, item)
        notifyItemMoved(position, nextPosition)
    }

    override fun onItemRemove(position: Int) {
        val item = items[position]
        dao.delete(item)
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    interface OnItemClickListener {
        fun onItemClick(node: String)
    }
}