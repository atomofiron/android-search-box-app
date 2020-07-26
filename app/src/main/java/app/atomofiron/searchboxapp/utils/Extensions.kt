package app.atomofiron.searchboxapp.utils

import android.view.View

fun View.setVisibility(visible: Boolean, invisibleMode: Int = View.GONE) {
    val visibility = if (visible) View.VISIBLE else invisibleMode
    if (this.visibility != visibility) {
        this.visibility = visibility
    }
}

fun String.escapeQuotes(): String = this.replace(Const.QUOTE, "\\" + Const.QUOTE)