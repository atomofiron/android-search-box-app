package app.atomofiron.searchboxapp

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.util.AttributeSet
import android.util.LayoutDirection
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import app.atomofiron.searchboxapp.utils.Const
import kotlin.math.max

val Fragment.anchorView: View get() = requireActivity().findViewById(R.id.joystick)

fun View.setContentMaxWidthRes(resId: Int) = setContentMaxWidth(resources.getDimensionPixelSize(resId))

fun View.setContentMaxWidth(value: Int) {
    var currentInset = 0
    addOnLayoutChangeListener { view, left, _, right, _, _, _, _, _ ->
        val width = right - left
        val inset = max(0, width - value) / 2
        val paddingLeft = paddingLeft - currentInset + inset
        val paddingRight = paddingRight - currentInset + inset
        currentInset = inset
        if (this.paddingLeft != paddingLeft || this.paddingRight != paddingRight) {
            view.updatePadding(left = paddingLeft, right = paddingRight)
        }
    }
}

inline fun Context.obtainStyledAttributes(
    attrs: AttributeSet?,
    @StyleableRes styleable: IntArray,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0,
    action: TypedArray.() -> Unit,
) {
    val styled = obtainStyledAttributes(attrs, styleable, defStyleAttr, defStyleRes)
    action(styled)
    styled.recycle()
}

val View.isLayoutRtl: Boolean get() = layoutDirection == View.LAYOUT_DIRECTION_RTL

fun View.isRtl(): Boolean = resources.isRtl()

fun Resources.isRtl(): Boolean = configuration.layoutDirection == LayoutDirection.RTL


fun RecyclerView.scrollToTop() {
    if (childCount == 0) return
    val topChild = getChildAt(0)
    val topHolder = getChildViewHolder(topChild)
    if (topHolder.absoluteAdapterPosition == 0) {
        smoothScrollToPosition(0)
        return
    }
    val spanCount = when (val manager = layoutManager) {
        is GridLayoutManager -> manager.spanCount
        is StaggeredGridLayoutManager -> manager.spanCount
        else -> 1
    }
    scrollToPosition(spanCount)
    post {
        smoothScrollToPosition(0)
    }
}

@SuppressLint("WrongConstant")
fun Context.updateNotificationChannel(
    id: String,
    name: String,
    importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT,
) {
    if (SDK_INT >= VERSION_CODES.O) {
        val manager = NotificationManagerCompat.from(this)
        var channel = manager.getNotificationChannel(id)
        if (channel == null || channel.name != name) {
            channel = NotificationChannel(id, name, importance)
            manager.createNotificationChannel(channel)
        }
    }
}

val ViewPager2.recyclerView: RecyclerView get() = getChildAt(0) as RecyclerView

fun Float.toIntAlpha(): Int = (Const.ALPHA_VISIBLE_INT * this).toInt().coerceIn(0, 255)

fun Int.setColorAlpha(alpha: Float): Int = setColorAlpha(alpha.toIntAlpha())

fun Int.setColorAlpha(alpha: Int): Int = ColorUtils.setAlphaComponent(this, alpha)

fun Int.asOverlayOn(background: Int): Int = ColorUtils.compositeColors(this, background)

