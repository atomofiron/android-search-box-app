package app.atomofiron.searchboxapp.custom

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.graphics.Insets
import androidx.core.view.*
import androidx.core.view.WindowInsetsCompat.Type
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView
import app.atomofiron.searchboxapp.custom.view.JoystickView
import app.atomofiron.searchboxapp.custom.view.SystemUiBackgroundView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.navigationrail.NavigationRailView

@SuppressLint("PrivateResource")
class OrientationLayoutDelegate constructor(
    private val parent: ViewGroup,
    private val explorerViews: Array<ExplorerView>? = null,
    private val recyclerView: RecyclerView? = null,
    private val bottomView: BottomNavigationView? = null,
    private val railView: NavigationRailView? = null,
    private val systemUiView: SystemUiBackgroundView? = null,
    private val tabLayout: MaterialButtonToggleGroup? = null,
    private val headerView: ExplorerHeaderView? = null,
) : OnApplyWindowInsetsListener {
    enum class Side(val isBottom: Boolean = false) {
        Left, Bottom(isBottom = true), Right,
    }

    private val resources = parent.resources
    private val railSize = resources.getDimensionPixelSize(R.dimen.m3_navigation_rail_default_width)
    private val bottomSize = resources.getDimensionPixelSize(R.dimen.m3_bottom_nav_min_height)
    private val navigationSize get() = if (side == Side.Bottom) bottomSize else railSize
    private val joystickSize get() = navigationSize
    private var side = Side.Bottom

    init {
        ViewCompat.setOnApplyWindowInsetsListener(parent, this)
        parent.setFabSideListener { side ->
            this.side = side
            setLayout(side != Side.Bottom)
            parent.requestApplyInsets()
        }
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        headerView?.run {
            ViewCompat.dispatchApplyWindowInsets(this, insets.getHeaderViewInsets())
        }
        recyclerView?.run {
            ViewCompat.dispatchApplyWindowInsets(this, insets.getRecyclerViewInsets())
        }
        railView?.run {
            ViewCompat.dispatchApplyWindowInsets(this, insets.getRailViewInsets())
        }
        tabLayout?.run {
            ViewCompat.dispatchApplyWindowInsets(this, insets.getTabLayoutInsets())
        }
        systemUiView?.run {
            ViewCompat.dispatchApplyWindowInsets(this, insets)
        }
        bottomView?.run {
            ViewCompat.dispatchApplyWindowInsets(this, insets)
        }
        explorerViews?.forEach {
            ViewCompat.dispatchApplyWindowInsets(it.headerView, insets.getHeaderViewInsets())
            ViewCompat.dispatchApplyWindowInsets(it.recyclerView, insets.getRecyclerViewInsets())
            ViewCompat.dispatchApplyWindowInsets(it.systemUiView, insets)
            it.onInsetsApplied()
        }
        return WindowInsetsCompat.CONSUMED
    }

    private fun setLayout(landscape: Boolean) {
        bottomView?.isVisible = !landscape
        railView?.isVisible = landscape
        tabLayout?.isVisible = !landscape
        explorerViews?.forEach {
            it.systemUiView.update(statusBar = landscape)
        }
    }

    private fun WindowInsetsCompat.getRecyclerViewInsets(): WindowInsetsCompat {
        return WindowInsetsCompat.Builder(this)
            .setInsets(insetsType, getInsets(insetsType).editForRecyclerView())
            .setInsets(Type.ime(), getInsets(Type.ime()).editForRecyclerView())
            .build()
    }

    private fun WindowInsetsCompat.getTabLayoutInsets(): WindowInsetsCompat {
        return WindowInsetsCompat.Builder(this)
            .setInsets(insetsType, getInsets(insetsType).editForTabLayout())
            .build()
    }

    private fun WindowInsetsCompat.getRailViewInsets(): WindowInsetsCompat {
        return WindowInsetsCompat.Builder(this)
            .setInsets(insetsType, getInsets(insetsType).editForRailView(joystickSize))
            .build()
    }

    private fun WindowInsetsCompat.getHeaderViewInsets(): WindowInsetsCompat {
        return WindowInsetsCompat.Builder(this)
            .setInsets(insetsType, getInsets(insetsType).editForHeaderView())
            .build()
    }

    private fun Insets.editForRecyclerView(): Insets {
        val top = when (side) {
            Side.Bottom -> if (tabLayout == null) top else 0
            Side.Left, Side.Right -> top
        }
        val bottom = when (side) {
            Side.Bottom -> bottom + navigationSize
            Side.Left, Side.Right -> bottom
        }
        val left = when (side) {
            Side.Left -> left + navigationSize
            Side.Bottom, Side.Right -> left
        }
        val right = when (side) {
            Side.Right -> right + navigationSize
            Side.Bottom, Side.Left -> right
        }
        return Insets.of(left, top, right, bottom)
    }

    private fun Insets.editForTabLayout(): Insets {
        val left = when (side) {
            Side.Left -> left + navigationSize
            Side.Bottom, Side.Right -> left
        }
        val right = when (side) {
            Side.Right -> right + navigationSize
            Side.Bottom, Side.Left -> right
        }
        return Insets.of(left, top, right, bottom)
    }

    private fun Insets.editForRailView(top: Int = 0): Insets {
        val left = when (side) {
            Side.Left, Side.Bottom -> left
            Side.Right -> 0
        }
        val right = when (side) {
            Side.Right, Side.Bottom -> right
            Side.Left -> 0
        }
        return Insets.of(left, this.top + top, right, bottom)
    }

    private fun Insets.editForHeaderView(): Insets {
        val left = when (side) {
            Side.Left -> left + navigationSize
            Side.Bottom,
            Side.Right -> left
        }
        val right = when (side) {
            Side.Right -> right + navigationSize
            Side.Left, Side.Bottom -> right
        }
        val top = when (side) {
            Side.Bottom -> 0
            Side.Left, Side.Right -> top
        }
        return Insets.of(left, top, right, bottom)
    }

    companion object {
        private val insetsType = Type.systemBars() or Type.displayCutout()

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