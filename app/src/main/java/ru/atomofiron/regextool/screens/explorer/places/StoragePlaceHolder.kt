package ru.atomofiron.regextool.screens.explorer.places

import android.view.View
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.DetailProgressView

class StoragePlaceHolder(itemView: View) : PlaceHolder(itemView) {
    companion object {
        private const val VISIBLE = 1f
        private const val INVISIBLE = 0.5f
    }

    private val dpvCapacity: DetailProgressView = requireViewById(R.id.place_dpv)

    init {
        dpvCapacity.findViewById<View>(R.id.progress_v_end).alpha = 0.3f
    }

    override fun onBind(item: XPlace, position: Int) {
        item as XPlace.StoragePlace
        super.onBind(item, position)
        dpvCapacity.set(100, 50, "100G", "50G", "49G")
        itemView.alpha = if (item.visible) VISIBLE else INVISIBLE
        ibAction.setImageResource(if (item.visible) R.drawable.ic_eye_striked else R.drawable.ic_eye)
    }
}