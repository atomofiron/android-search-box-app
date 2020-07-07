package ru.atomofiron.regextool.custom.view.bottom_sheet

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

    private var onShownCalled = false

    protected open fun show() {
        onShownCalled = false

        when (contentView) {
            null -> bottomSheetView.setView(layoutContent)
            else -> bottomSheetView.setView(contentView!!)
        }

        onViewReady()
        bottomSheetView.onClosedToOpenedListener = ::onSheetShown
        bottomSheetView.ifShownKeepOverlayVisible()
        bottomSheetView.post { bottomSheetView.show() }
    }

    protected fun hide() = bottomSheetView.hide()

    protected open fun onViewReady() = Unit

    protected open fun onViewShown() = Unit

    private fun onSheetShown() {
        // todo remove flag
        require(!onShownCalled) { }
        onShownCalled = true
        onViewShown()
    }
}