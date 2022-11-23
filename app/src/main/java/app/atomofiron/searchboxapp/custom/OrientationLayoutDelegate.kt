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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigationrail.NavigationRailView

@SuppressLint("PrivateResource")
class OrientationLayoutDelegate(
    private val parent: ViewGroup,
    private val recyclerView: RecyclerView,
    private val bottomView: BottomNavigationView? = null,
    private val railView: NavigationRailView? = null,
    private val headerView: ExplorerHeaderView? = null,
) : OnApplyWindowInsetsListener {
    enum class Side {
        Left, Bottom, Right,
    }

    private val resources = parent.resources
    private val railSize = resources.getDimensionPixelSize(R.dimen.m3_navigation_rail_default_width)
    private val bottomSize = resources.getDimensionPixelSize(R.dimen.m3_bottom_nav_min_height)
    private val navigationSize get() = if (side == Side.Bottom) bottomSize else railSize
    private val joystickSize get() = navigationSize
    private var side = Side.Bottom

    init {
        ViewCompat.setOnApplyWindowInsetsListener(parent, this)
        parent.setSideListener {
            val side = if (it) Side.Bottom else Side.Right
            this.side = side
            if (side == Side.Bottom) showBottom() else showRail()
            parent.requestApplyInsets()
        }
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val individual = when {
                child === headerView -> continue
                child === recyclerView -> continue
                child === railView -> continue
                else -> insets
            }
            ViewCompat.dispatchApplyWindowInsets(child, individual)
        }
        headerView?.run {
            ViewCompat.dispatchApplyWindowInsets(this, insets.getHeaderViewInsets())
        }
        recyclerView.run {
            ViewCompat.dispatchApplyWindowInsets(this, insets.getRecyclerViewInsets())
        }
        railView?.run {
            ViewCompat.dispatchApplyWindowInsets(this, insets.getRailViewInsets())
        }
        return WindowInsetsCompat.CONSUMED
    }

    private fun showRail() {
        bottomView?.isVisible = false
        railView?.isVisible = true
    }

    private fun showBottom() {
        bottomView?.isVisible = true
        railView?.isVisible = false
    }

    private fun WindowInsetsCompat.getRecyclerViewInsets(): WindowInsetsCompat {
        return WindowInsetsCompat.Builder(this)
            .setInsets(insetsType, getInsets(insetsType).editForRecyclerView())
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
        return Insets.of(left, top, right, bottom)
    }

    companion object {
        private val insetsType = Type.systemBars() or Type.displayCutout()

        fun ViewGroup.setSideListener(callback: (bottom: Boolean) -> Unit) {
            val maxSize = resources.getDimensionPixelSize(R.dimen.bottom_bar_max_width)
            addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
                val width = right - left
                val height = bottom - top
                when {
                    width < height && width < maxSize -> callback(true)
                    else -> callback(false)
                }
            }
        }

        fun JoystickView.syncOrientation(root: FrameLayout) {
            root.setSideListener { bottom ->
                updateLayoutParams<FrameLayout.LayoutParams> {
                    gravity = when {
                        bottom -> Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                        else -> Gravity.END or Gravity.TOP
                    }
                }
            }
        }
    }
}