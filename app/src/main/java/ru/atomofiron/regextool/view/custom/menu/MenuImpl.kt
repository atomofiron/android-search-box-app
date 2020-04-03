package ru.atomofiron.regextool.view.custom.menu

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.view.menu.MenuBuilder

@SuppressLint("RestrictedApi")
class MenuImpl(context: Context) : MenuBuilder(context) {

    private var menuChangedListener: (() -> Unit)? = null

    override fun onItemsChanged(structureChanged: Boolean) {
        super.onItemsChanged(structureChanged)
        menuChangedListener?.invoke()
    }

    fun setMenuChangedListener(listener: () -> Unit) {
        menuChangedListener = listener
    }

    // fix androidx.core.view.iterator()
    override fun removeItem(id: Int) = when {
        id >= 0 && id < size() -> removeItemAt(id)
        else -> super.removeItem(id)
    }
}