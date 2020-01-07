package ru.atomofiron.regextool.screens.explorer.adapter

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.recycler.GeneralHolder
import ru.atomofiron.regextool.common.util.Knife
import ru.atomofiron.regextool.iss.service.model.XFile

class ExplorerHolder(view: View) : GeneralHolder<XFile>(view) {

    private val icon = Knife<ImageView>(itemView, R.id.item_iv_icon)
    private val name = Knife<TextView>(itemView, R.id.item_tv_title)
    private val description = Knife<TextView>(itemView, R.id.item_tv_description)
    private val date = Knife<TextView>(itemView, R.id.item_tv_date)
    private val size = Knife<TextView>(itemView, R.id.item_tv_size)
    private val checkbox = Knife<CheckBox>(itemView, R.id.item_cb)

    var onItemClickListener: ((position: Int) -> Unit)? = null

    private var onClickListener: ((View) -> Unit) = {
        onItemClickListener?.invoke(adapterPosition)
    }

    override fun onBind(item: XFile, position: Int) {
        super.onBind(item, position)

        itemView.setOnClickListener(onClickListener)

        val type = when {
            !item.file.isDirectory -> R.drawable.ic_file_circle
            item.files?.isEmpty() == true -> R.drawable.ic_folder_empty
            else -> R.drawable.ic_folder
        }
        icon.view.setImageResource(type)
        name.view.text = item.file.name

        description.view.text = String.format("%s %s %s", item.access, item.owner, item.group)
        date.view.text = String.format("%s %s", item.date, item.time)
        size.view.text = when {
            item.file.isFile && item.size.length == 1 -> item.size + "B"
            item.file.isFile -> item.size
            else -> ""
        }
    }
}