package ru.atomofiron.regextool.view.custom.bottom_sheet

import android.view.View

abstract class BottomSheetDelegate(private val layoutContent: Int = UNDEFINED) {
    companion object {
        const val UNDEFINED = -1
    }
    lateinit var bottomSheetView: BottomSheetView
    protected open var contentView: View? = null
        set(value) {
            require(layoutContent == UNDEFINED) { Exception() }
            field = value
        }

    open fun show() {
        when (contentView) {
            null -> bottomSheetView.setView(layoutContent)
            else -> bottomSheetView.setView(contentView!!)
        }

        onViewReady()
        bottomSheetView.post { bottomSheetView.show() }
    }

    open fun onViewReady() = Unit
}