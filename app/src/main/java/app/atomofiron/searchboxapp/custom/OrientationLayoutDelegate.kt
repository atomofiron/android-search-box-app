package app.atomofiron.searchboxapp.custom

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.Insets
import androidx.core.view.*
import androidx.core.view.WindowInsetsCompat.Type
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView
import app.atomofiron.searchboxapp.custom.view.JoystickView
import app.atomofiron.searchboxapp.custom.view.SystemUiBackgroundView
import app.atomofiron.searchboxapp.isLayoutRtl
import app.atomofiron.searchboxapp.isRtl
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.navigation.NavigationView
import com.google.android.material.navigationrail.NavigationRailView
import lib.atomofiron.android_window_insets_compat.consumeInsets

@SuppressLint("PrivateResource")
class OrientationLayoutDelegate constructor(
    private val parent: ViewGroup,
    private val explorerViews: Array<ExplorerView>? = null,
    private val recyclerView: RecyclerView? = null,
    private val bottomView: BottomNavigationView? = null,
    private val railView: NavigationRailView? = null,
    private val systemUiView: SystemUiBackgroundView? = null,
    private val tabLayout: MaterialButtonToggleGroup? = null,
    private val appbarLayout: AppBarLayout? = null,
    private val sideDock: NavigationView? = null,
    private val headerView: ExplorerHeaderView? = null,
    private val snackbarContainer: CoordinatorLayout? = null,
    private val joystickVisibilityCallback: ((Boolean) -> Unit)? = null,
) : OnApplyWindowInsetsListener {
    enum class Side(val isBottom: Boolean = false) {
        Left, Bottom(isBottom = true), Right,
    }

    private val resources = parent.resources
    private val railSize = resources.getDimensionPixelSize(R.dimen.m3_navigation_rail_default_width)
    private val bottomSize = resources.getDimensionPixelSize(R.dimen.m3_bottom_nav_min_height)
    private val withBottomInset get() = side == Side.Bottom && (withJoystick || bottomView != null)
    private val withFlankInset get() = side != Side.Bottom && (withJoystick || railView != null)
    private val currentBottomSize get() = if (withBottomInset) bottomSize else 0
    private val currentRailSize get() = if (withFlankInset) railSize else 0
    private var side = Side.Bottom
    private var withJoystick = false

    init {
        ViewCompat.setOnApplyWindowInsetsListener(parent, this)
        setLayout(side != Side.Bottom)
        parent.setFabSideListener { side ->
            if (this.side == side) return@setFabSideListener
            this.side = side
            setLayout(side != Side.Bottom)
            parent.requestApplyInsets()
        }
        railView?.consumeInsets()
        sideDock?.consumeInsets()
    }

    override fun onApplyWindowInsets(parent: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        withJoystick = insets.joystickNeeded()
        joystickVisibilityCallback?.invoke(withJoystick)
        headerView?.let {
            ViewCompat.dispatchApplyWindowInsets(it, insets.getHeaderViewInsets())
        }
        recyclerView?.let {
            ViewCompat.dispatchApplyWindowInsets(it, insets.getRecyclerViewInsets())
        }
        railView?.applyToRail(insets)
        sideDock?.applyToSideDock(insets)
        tabLayout?.let {
            ViewCompat.dispatchApplyWindowInsets(it, insets.getTabLayoutInsets())
        }
        systemUiView?.let {
            ViewCompat.dispatchApplyWindowInsets(it, insets)
        }
        bottomView?.let {
            ViewCompat.dispatchApplyWindowInsets(it, insets)
        }
        appbarLayout?.let {
            ViewCompat.dispatchApplyWindowInsets(it, insets)
        }
        explorerViews?.forEach {
            ViewCompat.dispatchApplyWindowInsets(it.headerView, insets.getHeaderViewInsets())
            ViewCompat.dispatchApplyWindowInsets(it.recyclerView, insets.getRecyclerViewInsets())
            ViewCompat.dispatchApplyWindowInsets(it.systemUiView, insets)
            it.onInsetsApplied()
        }
        snackbarContainer?.let {
            ViewCompat.dispatchApplyWindowInsets(it, insets.getRecyclerViewInsets())
        }
        parent.applyToAppbar(insets)
        return WindowInsetsCompat.CONSUMED
    }

    private fun setLayout(landscape: Boolean) {
        bottomView?.isVisible = !landscape
        railView?.isVisible = landscape
        tabLayout?.isVisible = !landscape
        /* нужно только когда есть табы
        explorerViews?.forEach {
            it.systemUiView.update(statusBar = landscape)
        }*/
    }

    private fun WindowInsetsCompat.getRecyclerViewInsets(): WindowInsetsCompat {
        return custom {
            setInsets(insetsType, getInsets(insetsType).editForRecyclerView())
            setInsets(Type.ime(), getInsets(Type.ime()).editForRecyclerView())
        }
    }

    private fun WindowInsetsCompat.getTabLayoutInsets(): WindowInsetsCompat {
        return custom {
            setInsets(insetsType, getInsets(insetsType).editForTabLayout())
        }
    }

    private fun WindowInsetsCompat.getHeaderViewInsets(): WindowInsetsCompat {
        return custom {
            setInsets(insetsType, getInsets(insetsType).editForHeaderView())
        }
    }

    private inline fun WindowInsetsCompat.custom(action: WindowInsetsCompat.Builder.() -> Unit): WindowInsetsCompat {
        return WindowInsetsCompat.Builder(this).apply(action).build()
    }

    private fun Insets.editForRecyclerView(): Insets {
        val top = when (side) {
            Side.Bottom -> if (tabLayout == null) top else 0
            Side.Left, Side.Right -> top
        }
        val bottom = bottom + when (side) {
            Side.Bottom -> currentBottomSize
            Side.Left, Side.Right -> 0
        }
        val left = left + when (side) {
            Side.Left -> currentRailSize
            Side.Bottom, Side.Right -> 0
        }
        val right = right + when (side) {
            Side.Right -> currentRailSize
            Side.Bottom, Side.Left -> 0
        }
        return Insets.of(left, top, right, bottom)
    }

    private fun Insets.editForTabLayout(): Insets {
        val left = when (side) {
            Side.Left -> left + currentRailSize
            Side.Bottom, Side.Right -> left
        }
        val right = when (side) {
            Side.Right -> right + currentRailSize
            Side.Bottom, Side.Left -> right
        }
        return Insets.of(left, top, right, bottom)
    }

    private fun Insets.editForHeaderView(): Insets {
        val left = when (side) {
            Side.Left -> left + currentRailSize
            Side.Bottom,
            Side.Right -> left
        }
        val right = when (side) {
            Side.Right -> right + currentRailSize
            Side.Left, Side.Bottom -> right
        }
        val top = when (side) {
            Side.Bottom -> if (tabLayout == null) top else 0
            Side.Left, Side.Right -> top
        }
        return Insets.of(left, top, right, bottom)
    }

    private fun View.applyToAppbar(insets: WindowInsetsCompat) {
        val appbarLayout: AppBarLayout? = findViewById(R.id.appbar_layout)
        appbarLayout ?: return
        ViewCompat.dispatchApplyWindowInsets(appbarLayout, insets)

        val collapsingLayout: CollapsingToolbarLayout? = appbarLayout.findViewById(R.id.collapsing_layout)
        collapsingLayout ?: return
        val custom = insets.getAppbarLayoutInsets()
        val defaultMargin = resources.getDimensionPixelSize(R.dimen.m3_appbar_expanded_title_margin_horizontal)
        collapsingLayout.run {
            val start = defaultMargin + if (resources.isRtl()) custom.right else custom.left
            val end = defaultMargin + if (resources.isRtl()) custom.left else custom.right
            if (expandedTitleMarginStart != start)
                expandedTitleMarginStart = start
            if (expandedTitleMarginEnd != end)
                expandedTitleMarginEnd = end
        }

        val toolbar: Toolbar? = collapsingLayout.findViewById(R.id.toolbar)
        toolbar ?: return
        toolbar.updatePadding(left = custom.left, right = custom.right)
    }

    private fun NavigationRailView.applyToRail(windowInsets: WindowInsetsCompat) {
        val insets = windowInsets.getInsets(insetsType)
        val topInset = if (withJoystick) currentRailSize else 0
        val left = when (side) {
            Side.Left, Side.Bottom -> insets.left
            Side.Right -> 0
        }
        val right = when (side) {
            Side.Right, Side.Bottom -> insets.right
            Side.Left -> 0
        }
        updatePadding(left, insets.top + topInset, right, insets.bottom)
    }

    private fun NavigationView.applyToSideDock(windowInsets: WindowInsetsCompat) {
        var gravity = (layoutParams as? DrawerLayout.LayoutParams)?.gravity
        gravity = when {
            gravity == Gravity.END && isLayoutRtl -> Gravity.LEFT
            gravity == Gravity.END -> Gravity.RIGHT
            gravity == Gravity.START && isLayoutRtl -> Gravity.RIGHT
            gravity == Gravity.START -> Gravity.LEFT
            else -> gravity
        }
        val insets = windowInsets.getInsets(insetsType)
        val currentRailSize = if (withJoystick) currentRailSize else 0
        val left = when {
            gravity != Gravity.LEFT -> 0
            side == Side.Left -> insets.left + currentRailSize
            else -> insets.left
        }
        val right = when {
            gravity != Gravity.RIGHT -> 0
            side == Side.Right -> insets.right + currentRailSize
            else -> insets.right
        }
        updateLayoutParams {
            val minWidth = resources.getDimensionPixelSize(R.dimen.design_navigation_max_width)
            width = minWidth + left + right
        }
        updatePadding(left, insets.top, right, insets.bottom)
    }

    private fun WindowInsetsCompat.getAppbarLayoutInsets(): Insets {
        val cutout = getInsets(Type.displayCutout())
        val bars = getInsets(Type.navigationBars())
        var fab = Insets.of(
            if (side == Side.Left) railSize else 0,
            0,
            if (side == Side.Right) railSize else 0,
            if (side == Side.Bottom) bottomSize else 0,
        )
        fab = Insets.add(bars, fab)
        return Insets.max(fab, cutout)
    }

    companion object {
        private val insetsType = Type.systemBars() or Type.displayCutout()

        fun WindowInsetsCompat.joystickNeeded(): Boolean {
            val tappableElement = getInsets(Type.tappableElement())
            return getInsets(Type.navigationBars()).run {
                left > 0 && left != tappableElement.left
                        || bottom > 0 && bottom != tappableElement.bottom
                        || right > 0 && right != tappableElement.right
            }
        }

        fun ViewGroup.setFabSideListener(callback: (side: Side) -> Unit) {
            val maxSize = resources.getDimensionPixelSize(R.dimen.bottom_bar_max_width)
            addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
                val width = right - left
                val height = bottom - top
                val atTheBottom = width < height && width < maxSize
                when {
                    atTheBottom -> callback(Side.Bottom)
                    else -> callback(Side.Right)
                }
            }
        }

        fun JoystickView.syncOrientation(root: FrameLayout) {
            root.setFabSideListener { side ->
                updateLayoutParams<FrameLayout.LayoutParams> {
                    val flags = when (side) {
                        Side.Bottom -> Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                        else -> Gravity.END or Gravity.TOP
                    }
                    when (gravity and flags) {
                        flags -> return@setFabSideListener
                        else -> gravity = flags
                    }
                }
            }
        }
    }
}