package app.atomofiron.searchboxapp.screens.explorer.places

import android.view.View
import app.atomofiron.searchboxapp.R

enum class XPlaceType(val viewType: Int, val layoutId: Int) {
    InternalStorage(0, R.layout.item_place_storage) {
        override fun holder(itemView: View): PlaceHolder = StoragePlaceHolder(itemView)
    },
    ExternalStorage(1, R.layout.item_place_storage) {
        override fun holder(itemView: View): PlaceHolder = StoragePlaceHolder(itemView)
    },
    AnotherPlace(2, R.layout.item_place) {
        override fun holder(itemView: View): PlaceHolder = PlaceHolder(itemView)
    };

    abstract fun holder(itemView: View): PlaceHolder
}