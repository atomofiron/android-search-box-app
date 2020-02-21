package ru.atomofiron.regextool.view.custom

import android.content.Context
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.util.DrawerStateListenerImpl
import ru.atomofiron.regextool.utils.Const

class VerticalDockView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : NavigationView(context, attrs, defStyleAttr) {
    companion object {
        private const val PREFERENCE_GRAVITY_KEY = Const.PREF_DOCK_GRAVITY
    }
    private val ibDockSide: ImageButton
    val recyclerView: RecyclerView
    val isOpened: Boolean get() = drawerStateListener.isOpened

    private val gravity: Int get() = (layoutParams as? DrawerLayout.LayoutParams)?.gravity ?: Gravity.NO_GRAVITY
    private val drawerStateListener = DrawerStateListenerImpl()

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_drawer_navigation, this, true)

        recyclerView = findViewById(R.id.drawer_rv)

        ibDockSide = findViewById(R.id.drawer_ib_dock_side)
        ibDockSide.setOnClickListener {
            layoutParams = (layoutParams as? DrawerLayout.LayoutParams)?.apply {
                gravity = if (gravity == Gravity.START) Gravity.END else Gravity.START
                preferences.edit().putInt(PREFERENCE_GRAVITY_KEY, gravity).apply()
            }
        }

        val tvTitle = findViewById<TextView>(R.id.drawer_tv_title)
        val styled = context.obtainStyledAttributes(attrs, R.styleable.VerticalDockView, defStyleAttr, 0)
        tvTitle.text = styled.getString(R.styleable.VerticalDockView_title)
        styled.recycle()
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        super.setLayoutParams(params)

        val gravity = (params as? DrawerLayout.LayoutParams)?.gravity ?: Gravity.START
        ibDockSide.setImageResource(if (gravity == Gravity.START) R.drawable.ic_dock_end else R.drawable.ic_dock_start)
    }

    fun open() = (parent as DrawerLayout).openDrawer(gravity)

    fun close() = (parent as DrawerLayout).closeDrawer(gravity)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        (parent as DrawerLayout).addDrawerListener(drawerStateListener)

        layoutParams = (layoutParams as? DrawerLayout.LayoutParams)!!.apply {
            gravity = preferences.getInt(PREFERENCE_GRAVITY_KEY, Gravity.START)
        }
    }
}