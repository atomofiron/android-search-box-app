package ru.atomofiron.regextool.view.custom

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.view.custom.menu.MenuImpl

class BottomOptionMenu @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var onMenuItemClickListener: ((id: Int) -> Unit)? = null
    val menu = MenuImpl(context)

    init {
        menu.setMenuChangedListener(::onMenuChanged)
        orientation = HORIZONTAL
        // fix tab's ripple background
        setBackgroundColor(Color.TRANSPARENT)

        val styled = context.obtainStyledAttributes(attrs, R.styleable.BottomOptionMenu, defStyleAttr, 0)
        val menuId = styled.getResourceId(R.styleable.BottomOptionMenu_menu, 0)
        styled.recycle()

        if (menuId != 0) {
            val inflater = MenuInflater(context)
            inflater.inflate(menuId, menu)
        }

        completeMenu()
    }

    private fun onMenuChanged() {
        // todo wtf?
    }

    fun setOnMenuItemClickListener(listener: (id: Int) -> Unit) {
        onMenuItemClickListener = listener
    }

    private fun completeMenu() {
        val inflater = LayoutInflater.from(context)

        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val id = item.itemId
            val view = inflater.inflate(R.layout.item_bottom_tab, this, false) as ViewGroup
            view.findViewById<ImageView>(R.id.iv_icon).setImageDrawable(item.icon)
            view.findViewById<TextView>(R.id.tv_label).text = item.title
            view.setOnClickListener { onMenuItemClickListener?.invoke(id) }
            view.isEnabled = item.isEnabled
            view.clipToPadding = true
            addView(view)
        }
    }
}