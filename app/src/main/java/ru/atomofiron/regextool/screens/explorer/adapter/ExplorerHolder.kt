package ru.atomofiron.regextool.screens.explorer.adapter

import android.graphics.Typeface
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import app.atomofiron.common.recycler.GeneralHolder
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.iss.service.explorer.model.XFile
import ru.atomofiron.regextool.utils.Const

class ExplorerHolder(view: View) : GeneralHolder<XFile>(view) {
    companion object {
        private const val BYTE_LETTER = "B"
    }

    private val ivIcon = view.findViewById<ImageView>(R.id.item_iv_icon)
    private val tvName = view.findViewById<TextView>(R.id.item_tv_title)
    private val tvDescription = view.findViewById<TextView>(R.id.item_tv_description)
    private val tvDate = view.findViewById<TextView>(R.id.item_tv_date)
    private val tvSize = view.findViewById<TextView>(R.id.item_tv_size)
    private val cbFlag = view.findViewById<CheckBox>(R.id.item_cb)

    var onItemActionListener: ExplorerItemActionListener? = null

    private var onClickListener: ((View) -> Unit) = {
        onItemActionListener?.onItemClick(item)
    }

    private var onCheckListener: ((View) -> Unit) = { view ->
        view as CheckBox
        onItemActionListener?.onItemCheck(item, view.isChecked)
    }

    override fun onBind(item: XFile, position: Int) {
        super.onBind(item, position)

        itemView.setOnClickListener(onClickListener)
        cbFlag.setOnClickListener(onCheckListener)

        val image = when {
            !item.isDirectory -> R.drawable.ic_file_circle
            item.files?.isEmpty() == true -> R.drawable.ic_explorer_folder_empty
            else -> R.drawable.ic_explorer_folder
        }
        ivIcon.setImageResource(image)
        ivIcon.alpha = if (item.isDirectory && !item.isCached) .5f else 1f

        tvName.text = if (item.completedPath == Const.ROOT) Const.ROOT else item.name
        tvName.typeface = if (item.isDirectory) Typeface.DEFAULT_BOLD else Typeface.DEFAULT

        tvDescription.text = String.format("%s %s %s", item.access, item.owner, item.group)
        tvDate.text = String.format("%s %s", item.date, item.time)
        tvSize.text = when {
            item.isFile && item.size.length == 1 -> item.size + BYTE_LETTER
            item.isFile -> item.size
            else -> ""
        }

        cbFlag.isChecked = item.isChecked
        cbFlag.visibility = if (item.isRoot) View.INVISIBLE else View.VISIBLE
    }
}