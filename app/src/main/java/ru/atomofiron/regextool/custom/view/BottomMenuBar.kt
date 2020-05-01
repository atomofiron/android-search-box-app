package ru.atomofiron.regextool.custom.view

import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import app.atomofiron.common.util.findColorByAttr
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.menu.MenuImpl

class BottomMenuBar @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    companion object {
        private const val ALPHA_ENABLED = 1f
        private const val ALPHA_DISABLED = 0.3f
    }

    private var onMenuItemClickListener: ((id: Int) -> Unit)? = null
    val menu = MenuImpl(context)

    init {
        menu.setMenuChangedListener(::onMenuChanged)
        orientation = HORIZONTAL
        // fix tab's ripple background
        setBackgroundColor(context.findColorByAttr(R.attr.colorBackground))

        val styled = context.obtainStyledAttributes(attrs, R.styleable.BottomMenuBar, defStyleAttr, 0)
        val menuId = styled.getResourceId(R.styleable.BottomMenuBar_menu, 0)
        styled.recycle()

        if (menuId != 0) {
            val inflater = MenuInflater(context)
            inflater.inflate(menuId, menu)
        }

        completeMenu()
    }

    private fun onMenuChanged() {
        for (index in 0 until menu.size()) {
            val item = menu.getItem(index)
            val view = findViewById<View>(item.itemId)
            view ?: continue
            updateItem(view, item)
        }
        for (index in 0 until childCount) {
            val view = getChildAt(index)
            val item = menu.findItem(view.id)
            if (item == null) {
                view.findViewById<ImageView>(R.id.iv_icon).setImageDrawable(null)
                view.findViewById<TextView>(R.id.tv_label).text = null
                view.isEnabled = false
                view.isFocusable = false
            }
        }
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
            view.clipToPadding = true
            view.id = id
            updateItem(view, item)
            addView(view)
        }
    }

    private fun updateItem(view: View, item: MenuItem) {
        view.alpha = if (item.isEnabled) ALPHA_ENABLED else ALPHA_DISABLED
        view.isEnabled = item.isEnabled
    }
}