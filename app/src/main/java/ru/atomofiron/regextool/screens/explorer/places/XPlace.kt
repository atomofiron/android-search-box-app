package ru.atomofiron.regextool.screens.explorer.places

import android.view.View
import ru.atomofiron.regextool.R

@Suppress("EqualsOrHashCode")
sealed class XPlace(val stableId: Long, val enumType: XPlaceType, val icon: Int, val title: String, val iconAction: Int) {
    abstract class StoragePlace(stableId: Long, enumType: XPlaceType, icon: Int, title: String, val visible: Boolean) : XPlace(
            stableId, enumType, icon, title, 0
    )
    class InternalStorage(title: String, visible: Boolean) : StoragePlace(
            1L, XPlaceType.InternalStorage, R.drawable.ic_slash, title, visible
    ) {
        override fun holder(itemView: View): PlaceHolder = PlaceHolder(itemView)

        override fun hashCode(): Int = visible.hashCode() + R.drawable.ic_slash
    }
    class ExternalStorage(title: String, visible: Boolean) : StoragePlace(
            2L, XPlaceType.ExternalStorage, R.drawable.ic_sdcard, title, visible
    ) {
        override fun holder(itemView: View): PlaceHolder = PlaceHolder(itemView)

        override fun hashCode(): Int = visible.hashCode() + R.drawable.ic_sdcard
    }
    class AnotherPlace(title: String) : XPlace(
            title.hashCode().toLong(), XPlaceType.AnotherPlace, R.drawable.ic_place_folder, title, R.drawable.ic_cross
    ) {
        override fun holder(itemView: View): PlaceHolder = PlaceHolder(itemView)

        override fun hashCode(): Int = title.hashCode()
    }

    abstract fun holder(itemView: View): PlaceHolder

    override fun equals(other: Any?): Boolean {
        return when (other) {
            !is XPlace -> false
            else -> other.stableId == stableId
        }
    }
}