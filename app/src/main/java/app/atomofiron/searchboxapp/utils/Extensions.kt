package app.atomofiron.searchboxapp.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainPresenterParams

fun String.escapeQuotes(): String = this.replace(Const.QUOTE, "\\" + Const.QUOTE)

fun Context.getMarketIntent() = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))

fun BaseRouter.showCurtain(recipient: String, layoutId: Int) {
    navigation {
        val args = CurtainPresenterParams.args(recipient, layoutId)
        navigate(R.id.curtainFragment, args, BaseRouter.curtainOptions)
    }
}

fun Int.immutable(): Int = when {
    SDK_INT >= M -> this or PendingIntent.FLAG_IMMUTABLE
    else -> this
}

inline fun <E> Iterable<E>.findIndexed(predicate: (E) -> Boolean): Pair<Int, E?> {
    for ((index, item) in this.withIndex()) {
        if (predicate(item)) return index to item
    }
    return -1 to null
}

fun Resources.getString(error: NodeError): String {
    return when (error) {
        is NodeError.NoSuchFile -> getString(R.string.no_such_file)
        is NodeError.PermissionDenied -> getString(R.string.permission_denied)
        is NodeError.Unknown -> getString(R.string.unknown_error)
        is NodeError.Multiply -> getString(R.string.a_lot_of_errors)
        is NodeError.Message -> error.message
    }
}
