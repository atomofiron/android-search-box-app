package app.atomofiron.searchboxapp.custom.view.bottom_sheet

import android.content.Context
import android.view.View

abstract class BottomSheetDelegate(private val layoutContent: Int = UNDEFINED) {
    companion object {
        const val UNDEFINED = -1
    }
    lateinit var bottomSheetView: BottomSheetView
    protected val context: Context get() = bottomSheetView.context
    protected open var contentView: View? = null
        set(value) {
            require(layoutContent == UNDEFINED) { Exception() }
            field = value
        }

    private var onShownCalled = false
    private var onHiddenCalled = false
    private val onExpandedListener = ::onStateShanged

    protected open fun show() {
        onShownCalled = false
        onHiddenCalled = false

        when (contentView) {
            null -> bottomSheetView.setView(layoutContent)
            else -> bottomSheetView.setView(contentView!!)
        }

        onViewReady()
        bottomSheetView.ifShownKeepOverlayVisible()
        bottomSheetView.post {
            bottomSheetView.stateListener = onExpandedListener
            bottomSheetView.show()
        }
    }

    protected fun hide() = bottomSheetView.hide()

    protected open fun onViewReady() = Unit

    protected open fun onViewShown() = Unit

    protected open fun onViewHidden() = Unit

    private fun onStateShanged(shown: Boolean) {
        when {
            shown -> if (!onShownCalled) {
                onShownCalled = true
                onViewShown()
            }
            else -> if (!onHiddenCalled) {
                onHiddenCalled = true
                onViewHidden()
            }
        }
    }
}