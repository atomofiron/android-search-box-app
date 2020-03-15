package ru.atomofiron.regextool.screens.explorer.places

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import app.atomofiron.common.recycler.GeneralHolder
import com.google.android.material.textview.MaterialTextView
import ru.atomofiron.regextool.R

open class PlaceHolder(itemView: View) : GeneralHolder<XPlace>(itemView) {
    protected val llRoot: LinearLayout = requireViewById(R.id.place_ll_root)
    protected val mtvTitle: MaterialTextView = requireViewById(R.id.place_mtv_title)
    protected val ivIcon: ImageView = requireViewById(R.id.place_iv_icon)
    protected val ibAction: ImageButton = requireViewById(R.id.place_ib_action)

    var itemActionListener: ItemActionListener? = null

    init {
        llRoot.setOnClickListener {
            itemActionListener?.onItemClick(item)
        }
        ibAction.setOnClickListener {
            itemActionListener?.onItemActionClick(item)
        }
    }

    override fun onBind(item: XPlace, position: Int) {
        mtvTitle.text = item.title
        ivIcon.setImageResource(item.icon)
        if (item.iconAction != 0) {
            ibAction.setImageResource(item.iconAction)
        }
    }

    interface ItemActionListener {
        fun onItemClick(item: XPlace)
        fun onItemActionClick(item: XPlace)
    }
}