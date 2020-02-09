package ru.atomofiron.regextool.screens.finder.history.adapter

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cursoradapter.widget.CursorAdapter
import ru.atomofiron.regextool.R
import java.util.*

class VocabCursorAdapter(
        context: Context?,
        c: Cursor,
        private val onItemClickListener: HistoryAdapter.OnItemClickListener
) : CursorAdapter(context, c, true) {
    companion object {
        fun create(context: Context, onItemClickListener: HistoryAdapter.OnItemClickListener): CursorAdapter {

            val myDB: SQLiteDatabase = context.openOrCreateDatabase("history.db", Context.MODE_PRIVATE, null)
            myDB.execSQL("create table if not exists History (_id Integer PRIMARY KEY AUTOINCREMENT, string Text, pinned Boolean);")
            val cursor = myDB.query("History", null, null, null, null, null, "pinned")
            return VocabCursorAdapter(context, cursor, onItemClickListener)
        }
    }

    private val itemChecked = ArrayList<Boolean>()
    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(context)
        return inflater.inflate(R.layout.item_history, parent, false)
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val position = cursor.position
        // get position by cursor
        val checkBox = view.findViewById<View>(R.id.item_history_btn_pinned)
        checkBox.setOnClickListener {
            itemChecked[position] = !itemChecked[position]
        }
        checkBox.isActivated = itemChecked[position]
        // set the checkbox state base on arraylist object state
        Log.i("In VocabCursorAdapter", "position: " + position + " - checkbox state: " + itemChecked[position])
    }

    // array list for store state of each checkbox
    init {
        for (i in 0 until c.count) { // c.getCount() return total number of your Cursor
            itemChecked.add(i, false)
            // initializes all items value with false
        }
    }
}