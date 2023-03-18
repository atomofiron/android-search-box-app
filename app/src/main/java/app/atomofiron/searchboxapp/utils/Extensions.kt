package app.atomofiron.searchboxapp.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.os.Build.VERSION_CODES.M
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.forEachIndexed
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.drawable.BallsDrawable.Companion.setBallsDrawable
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainPresenterParams
import com.google.android.material.bottomnavigation.BottomNavigationView

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

fun Resources.getString(error: NodeError, content: NodeContent? = null): String {
    return when (error) {
        is NodeError.NoSuchFile -> when (content) {
            is NodeContent.Directory -> getString(R.string.no_such_directory)
            is NodeContent.File -> getString(R.string.no_such_file)
            else -> getString(R.string.no_such_file_or_directory)
        }
        is NodeError.PermissionDenied -> getString(R.string.permission_denied)
        is NodeError.Unknown -> getString(R.string.unknown_error)
        is NodeError.Multiply -> getString(R.string.a_lot_of_errors)
        is NodeError.Message -> error.message
    }
}

const val DEFAULT_FREQUENCY = 60

fun Context.getFrequency(): Int {
    val refreshRate = when {
        SDK_INT >= VERSION_CODES.R -> display?.refreshRate
        else -> {
            val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager?
            manager?.defaultDisplay?.refreshRate
        }
    }
    return refreshRate?.toInt() ?: DEFAULT_FREQUENCY
}

fun BottomNavigationView.setProgressItem(itemId: Int, iconId: Int, title: String?, enabled: Boolean? = null) {
    setProgressItem(itemId, iconId, null, title, enabled)
}

fun BottomNavigationView.setProgressItem(itemId: Int, icon: Drawable, title: String?, enabled: Boolean? = null) {
    setProgressItem(itemId, 0, icon, title, enabled)
}

private fun BottomNavigationView.setProgressItem(itemId: Int, iconId: Int, icon: Drawable?, title: String?, enabled: Boolean?) {
    val menuView = getChildAt(0) as ViewGroup
    menu.forEachIndexed { index, item ->
        if (item.itemId != itemId) return@forEachIndexed
        val itemView = menuView.getChildAt(index)
        val drawable = when {
            icon != null -> icon
            iconId == R.drawable.progress_loop -> {
                val iv = itemView.findViewById<ImageView>(R.id.navigation_bar_item_icon_view)
                iv.setBallsDrawable()
            }
            else -> ContextCompat.getDrawable(context, iconId)
        }
        enabled?.let { item.isEnabled = enabled }
        item.icon = drawable
        item.title = title
    }
}

fun Drawable.updateState(enabled: Boolean? = null, checked: Boolean? = null, activated: Boolean? = null) {
    val flags = getStateMut(enabled, checked, activated)
    for (flag in state) {
        if (!flags.contains(flag) && !flags.contains(-flag)) {
            flags.add(flag)
        }
    }
    state = flags.toIntArray()
}

fun Drawable.setState(enabled: Boolean? = null, checked: Boolean? = null, activated: Boolean? = null) {
    state = getStateMut(enabled, checked, activated).toIntArray()
}

private fun getStateMut(
    enabled: Boolean? = null,
    checked: Boolean? = null,
    activated: Boolean? = null,
): MutableList<Int> {
    val flags = mutableListOf<Int>()
    enabled?.let { flags.add(android.R.attr.state_enabled * it.toInt()) }
    checked?.let { flags.add(android.R.attr.state_checked * it.toInt()) }
    activated?.let { flags.add(android.R.attr.state_activated * it.toInt()) }
    return flags
}

fun Boolean.toInt(): Int = if (this) 1 else -1
