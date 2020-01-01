package ru.atomofiron.regextool.view.custom.bottom_sheet_menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.Util.log

class BottomSheetViewAdapter : RecyclerView.Adapter<BottomSheetViewAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_bottom_sheet_menu, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int = 5

    override fun onBindViewHolder(holder: Holder, position: Int) {
        log("onBindViewHolder")
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view)
}