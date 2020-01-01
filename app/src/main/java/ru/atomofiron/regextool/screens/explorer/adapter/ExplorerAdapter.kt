package ru.atomofiron.regextool.screens.explorer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.recycler.GeneralAdapter
import ru.atomofiron.regextool.common.recycler.GeneralHolder
import java.io.File

class ExplorerAdapter : GeneralAdapter<ExplorerAdapter.ExplorerHolder, File>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExplorerHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_explorer_file, parent, false)
        return ExplorerHolder(view)
    }

    class ExplorerHolder(view: View) : GeneralHolder<File>(view)
}