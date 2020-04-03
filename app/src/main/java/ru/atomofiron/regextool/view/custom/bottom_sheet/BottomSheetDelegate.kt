package ru.atomofiron.regextool.view.custom.bottom_sheet

import android.view.View

abstract class BottomSheetDelegate(private val layoutContent: Int) {
    companion object {
        private const val UNDEFINED = -1
    }
    lateinit var bottomSheetView: BottomSheetView
    open var contentView: View? = null
        set(value) {
            require(layoutContent == UNDEFINED) { Exception() }
            field = value
        }

    constructor() : this(UNDEFINED)

    fun show() {
        when (contentView) {
            null -> bottomSheetView.setView(layoutContent)
            else -> bottomSheetView.setView(contentView!!)
        }

        bottomSheetView.post { bottomSheetView.show() }
    }
}