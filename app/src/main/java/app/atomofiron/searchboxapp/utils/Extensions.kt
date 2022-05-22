package app.atomofiron.searchboxapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainPresenterParams

fun View.setVisibility(visible: Boolean, invisibleMode: Int = View.GONE) {
    val visibility = if (visible) View.VISIBLE else invisibleMode
    if (this.visibility != visibility) {
        this.visibility = visibility
    }
}

fun String.escapeQuotes(): String = this.replace(Const.QUOTE, "\\" + Const.QUOTE)

fun Context.getMarketIntent() = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))

fun BaseRouter.showCurtain(recipient: String, layoutId: Int) {
    navigation {
        val args = CurtainPresenterParams.args(recipient, layoutId)
        navigate(R.id.curtainFragment, args, BaseRouter.curtainOptions)
    }
}