package ru.atomofiron.regextool.view.custom.bottom_sheet_menu

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimationUtilsCompat
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.util.findColorByAttr
import ru.atomofiron.regextool.utils.loadAnimationWithDurationScale

class BottomSheetView : FrameLayout, Animation.AnimationListener {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        overlay = View(context)
        overlay.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        overlay.setBackgroundColor(Color.parseColor("#22000000"))
        addView(overlay)

        menu = RecyclerView(context)
        menu.layoutParams = LayoutParams(MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
        }
        menu.layoutManager = LinearLayoutManager(context)
        menu.adapter = BottomSheetViewAdapter()
        //menu.setBackgroundColor(context.findColorByAttr(R.attr.colorBackground))
        menu.setBackgroundResource(R.drawable.bg_bottom_sheet)
        val top = resources.getDimensionPixelSize(R.dimen.menu_corner_radius)
        val bottom = resources.getDimensionPixelSize(R.dimen.action_bar_size)
        menu.setPadding(0, top, 0, bottom)
        menu.clipToPadding = false
        addView(menu)

        menuHideAnimation.setAnimationListener(this)
        visibility = View.GONE
    }

    private val overlay: View
    private val menu: RecyclerView

    private val overlayShowAnimation = context.loadAnimationWithDurationScale(R.anim.alpha_show)
    private val overlayHideAnimation = context.loadAnimationWithDurationScale(R.anim.alpha_hide)
    private val menuShowAnimation = context.loadAnimationWithDurationScale(R.anim.translate_up_show)
    private val menuHideAnimation = context.loadAnimationWithDurationScale(R.anim.translate_up_hide)

    var isSheetShown: Boolean = false
    private set

    fun show() {
        isSheetShown = true

        overlay.animation?.cancel()
        overlay.startAnimation(overlayShowAnimation)
        menu.animation?.cancel()
        menu.startAnimation(menuShowAnimation)

        isClickable = true
        isFocusable = true
        visibility = View.VISIBLE
        setOnClickListener { hide() }
        menu.setOnClickListener { hide() }
    }

    fun hide() {
        isSheetShown = false

        overlay.animation?.cancel()
        overlay.startAnimation(overlayHideAnimation)
        menu.animation?.cancel()
        menu.startAnimation(menuHideAnimation)

        isClickable = false
        isFocusable = false
        setOnClickListener(null)
    }

    override fun onAnimationStart(animation: Animation?) = Unit
    override fun onAnimationRepeat(animation: Animation?) = Unit
    override fun onAnimationEnd(animation: Animation?) {
        visibility = View.GONE
    }
}