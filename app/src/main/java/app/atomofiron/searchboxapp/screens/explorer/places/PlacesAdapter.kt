package app.atomofiron.searchboxapp.screens.explorer.places

import android.view.LayoutInflater
import android.view.ViewGroup
import app.atomofiron.common.recycler.GeneralAdapter

class PlacesAdapter : GeneralAdapter<PlaceHolder, XPlace>() {
    var itemActionListener: ItemActionListener? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    init {
        setHasStableIds(true)
    }

    override fun getItemViewType(position: Int): Int =  items[position].enumType.viewType

    override fun getItemId(position: Int): Long = items[position].stableId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): PlaceHolder {
        val enumType = XPlaceType.values()[viewType]
        val itemView = inflater.inflate(enumType.layoutId, parent, false)
        return enumType.holder(itemView)
    }

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemActionListener = itemActionListener
    }

    interface ItemActionListener : PlaceHolder.ItemActionListener
}