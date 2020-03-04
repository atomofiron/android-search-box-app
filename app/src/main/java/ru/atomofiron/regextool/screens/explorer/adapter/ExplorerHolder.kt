package ru.atomofiron.regextool.screens.explorer.adapter

import android.graphics.Typeface
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import ru.atomofiron.regextool.R
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.common.util.Knife
import ru.atomofiron.regextool.iss.service.model.XFile
import ru.atomofiron.regextool.utils.Const

class ExplorerHolder(view: View) : GeneralHolder<XFile>(view) {
    companion object {
        private const val BYTE_LETTER = "B"
    }

    private val icon = Knife<ImageView>(itemView, R.id.item_iv_icon)
    private val name = Knife<TextView>(itemView, R.id.item_tv_title)
    private val description = Knife<TextView>(itemView, R.id.item_tv_description)
    private val date = Knife<TextView>(itemView, R.id.item_tv_date)
    private val size = Knife<TextView>(itemView, R.id.item_tv_size)
    private val checkbox = Knife<CheckBox>(itemView, R.id.item_cb)

    var onItemActionListener: ItemActionListener? = null

    private var onClickListener: ((View) -> Unit) = {
        onItemActionListener?.onItemClick(item)
    }

    override fun onBind(item: XFile, position: Int) {
        super.onBind(item, position)

        itemView.setOnClickListener(onClickListener)

        val image = when {
            !item.isDirectory -> R.drawable.ic_file_circle
            item.files?.isEmpty() == true -> R.drawable.ic_folder_empty
            else -> R.drawable.ic_folder
        }
        icon {
            setImageResource(image)
            alpha = if (item.isDirectory && !item.isCached) .5f else 1f
        }
        name {
            text = if (item.completedPath == Const.ROOT) Const.ROOT else item.name
            typeface = if (item.isDirectory) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }

        description.view.text = String.format("%s %s %s", item.access, item.owner, item.group)
        date.view.text = String.format("%s %s", item.date, item.time)
        size.view.text = when {
            item.isFile && item.size.length == 1 -> item.size + BYTE_LETTER
            item.isFile -> item.size
            else -> ""
        }
    }
}