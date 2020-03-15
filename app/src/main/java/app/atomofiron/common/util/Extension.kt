package app.atomofiron.common.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat

fun View.showKeyboard() {
    this.requestFocus()
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, 0)
}

fun View.hideKeyboard() {
    this.clearFocus()
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    inputMethodManager?.hideSoftInputFromWindow(windowToken, 0)
}


fun Context.findResIdByAttr(@AttrRes attr: Int): Int = findResIdsByAttr(attr)[0]

fun Context.findResIdsByAttr(@AttrRes vararg attrs: Int): IntArray {
    @SuppressLint("ResourceType")
    val array = obtainStyledAttributes(attrs)

    val resIds = IntArray(attrs.size)
    for (i in attrs.indices) {
        resIds[i] = array.getResourceId(i, 0)
    }
    array.recycle()

    return resIds
}

fun Context?.findBooleanByAttr(@AttrRes attr: Int): Boolean = findBooleansByAttr(attr)[0]

fun Context?.findBooleansByAttr(@AttrRes vararg attrs: Int): BooleanArray {
    @SuppressLint("ResourceType")
    val array = this!!.obtainStyledAttributes(attrs)

    val values = BooleanArray(attrs.size)
    for (i in attrs.indices) {
        values[i] = array.getBoolean(i, false)
    }
    array.recycle()

    return values
}

fun Context?.findDimenByAttr(@AttrRes attr: Int): Int = findDimensByAttr(attr)[0]

fun Context?.findDimensByAttr(@AttrRes vararg attrs: Int): IntArray {
    @SuppressLint("ResourceType")
    val array = this!!.obtainStyledAttributes(attrs)

    val values = IntArray(attrs.size)
    for (i in attrs.indices) {
        values[i] = array.getDimensionPixelOffset(i, 0)
    }
    array.recycle()

    return values
}

@ColorInt
fun Context.findColorByAttr(@AttrRes attr: Int) = ContextCompat.getColor(this, findResIdByAttr(attr))

fun ViewGroup.moveChildrenFrom(layoutId: Int) {
    val inflater = LayoutInflater.from(context)
    val container = inflater.inflate(layoutId, null, false) as ViewGroup
    for (i in 0 until container.childCount) {
        val child = container.getChildAt(0)
        container.removeView(child)
        addView(child)
    }
}
