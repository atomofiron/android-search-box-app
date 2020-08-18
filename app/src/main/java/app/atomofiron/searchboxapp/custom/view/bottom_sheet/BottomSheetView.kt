package app.atomofiron.searchboxapp.custom.view.bottom_sheet

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import app.atomofiron.common.util.findResIdByAttr
import app.atomofiron.common.util.moveChildrenFrom
import com.google.android.material.bottomsheet.BottomSheetBehavior
import app.atomofiron.searchboxapp.R
import kotlin.math.min

class BottomSheetView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr) {
    companion object {
        private const val CONTENT_VIEW_INDEX = 1
        private const val UNDEFINED = -1
    }

    init {
        moveChildrenFrom(R.layout.view_bottom_sheet)
    }

    private val overlay: View = findViewById(R.id.bottom_sheet_overlay)
    private val viewContainer: ViewGroup = findViewById(R.id.bottom_container)
    val anchorView: View get() = viewContainer
    private val behavior = (viewContainer.layoutParams as LayoutParams).behavior as BottomSheetBehavior<View>
    lateinit var contentView: View private set
    private var contentViewId: Int = UNDEFINED
    private var behaviorTargetState = BottomSheetBehavior.STATE_HIDDEN

    val isSheetShown: Boolean get() = when (behavior.state) {
        BottomSheetBehavior.STATE_EXPANDED -> true
        BottomSheetBehavior.STATE_SETTLING -> true
        BottomSheetBehavior.STATE_DRAGGING -> true
        else -> false
    }

    var stateListener: (shown: Boolean) -> Unit = { }
    private var keepOverlayVisible = false
    private val bottomSheetCallback = BottomSheetCallback()

    init {
        val widthPixels = resources.displayMetrics.widthPixels
        val heightPixels = resources.displayMetrics.heightPixels
        val sizePixels = min(widthPixels, heightPixels)
        val maxWidth = resources.getDimensionPixelOffset(R.dimen.bottom_sheet_view_max_width)
        viewContainer.layoutParams.width = min(sizePixels, maxWidth)

        behavior.addBottomSheetCallback(bottomSheetCallback)
        overlay.setOnClickListener(::onOverlayClick)
        overlay.isClickable = false
        overlay.isFocusable = false
        hide()
    }

    private fun onOverlayClick(v: View) {
        if (isSheetShown) {
            hide()
        }
    }

    private fun clearContainer() {
        while (viewContainer.childCount > CONTENT_VIEW_INDEX) {
            viewContainer.removeViewAt(CONTENT_VIEW_INDEX)
        }
    }

    fun setView(id: Int): View {
        if (id != contentViewId) {
            clearContainer()
            LayoutInflater.from(context).inflate(id, viewContainer)
            setView(viewContainer.getChildAt(CONTENT_VIEW_INDEX))
            contentViewId = id
        }
        return contentView
    }

    fun setView(view: View) {
        when  {
            !::contentView.isInitialized -> Unit
            contentView === view -> return
            viewContainer.getChildAt(CONTENT_VIEW_INDEX) !== view -> clearContainer()
            else -> Unit
        }
        contentViewId = UNDEFINED
        contentView = view

        val tag = context.getString(R.string.bottom_sheet_main_view_tag)
        val mainView = view.findViewWithTag(tag) ?: view

        var paddingBottom = resources.getDimensionPixelSize(R.dimen.bottom_tab_bar_height)
        paddingBottom = when (paddingBottom > mainView.paddingBottom) {
            true -> mainView.paddingBottom + paddingBottom
            false -> mainView.paddingBottom
        }
        mainView.setPadding(mainView.paddingLeft, mainView.paddingTop, mainView.paddingRight, paddingBottom)

        when {
            view.parent == null -> viewContainer.addView(view)
            view.parent === viewContainer -> Unit
            else -> throw Exception("View already has another parent!")
        }
    }

    fun ifShownKeepOverlayVisible() {
        keepOverlayVisible = isSheetShown
    }

    fun show() {
        behaviorTargetState = BottomSheetBehavior.STATE_EXPANDED
        behavior.state = behaviorTargetState
    }

    /** @return sheet was opened */
    fun hide(): Boolean {
        behaviorTargetState = BottomSheetBehavior.STATE_HIDDEN
        behavior.state = behaviorTargetState
        return isSheetShown
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        bottomSheetCallback.updateBackGround(behavior.state)

        val isSettling = behavior.state == BottomSheetBehavior.STATE_SETTLING
        if (isSettling && changed) {
            behavior.state = behaviorTargetState
        }
    }

    private inner class BottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {
        private val colorBackground = overlay.context.findResIdByAttr(R.attr.colorBackground)

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val isExpanded = slideOffset.isNaN()
            val value = when {
                keepOverlayVisible -> 1f
                isExpanded -> 1f
                else -> 1 + slideOffset
            }
            overlay.alpha = value
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            val isStateHidden = newState == BottomSheetBehavior.STATE_HIDDEN
            if (overlay.isClickable == isStateHidden) {
                overlay.isClickable = !isStateHidden
                overlay.isFocusable = !isStateHidden
            }

            updateBackGround(newState)

            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    keepOverlayVisible = false
                    stateListener.invoke(true)
                }
                BottomSheetBehavior.STATE_HIDDEN -> stateListener.invoke(false)
            }
        }

        fun updateBackGround(state: Int) {
            val resource = when {
                viewContainer.top != 0 -> R.drawable.bottom_sheet_corners
                state != BottomSheetBehavior.STATE_EXPANDED -> R.drawable.bottom_sheet_corners
                else -> colorBackground
            }
            viewContainer.setBackgroundResource(resource)
        }
    }
}