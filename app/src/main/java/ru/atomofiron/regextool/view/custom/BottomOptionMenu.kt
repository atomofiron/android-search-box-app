package ru.atomofiron.regextool.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.util.findBooleanByAttr
import ru.atomofiron.regextool.view.custom.menu.MenuImpl

class BottomOptionMenu : LinearLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        menu.setMenuChangedListener(::onMenuChanged)

        val styled = context.obtainStyledAttributes(attrs, R.styleable.BottomOptionMenu, defStyleAttr, 0)
        val menuId = styled.getResourceId(R.styleable.BottomOptionMenu_menu, 0)
        styled.recycle()

        if (menuId != 0) {
            val inflater = MenuInflater(context)
            inflater.inflate(menuId, menu)
        }

        completeMenu()
        orientation = HORIZONTAL
        isClickable = false
        isFocusable = false

    }

    val menu = MenuImpl(context)
    private var onMenuItemClickListener: ((id: Int) -> Unit)? = null

    private fun onMenuChanged() {
        // todo
    }

    fun setOnMenuItemClickListener(listener: (id: Int) -> Unit) {
        onMenuItemClickListener = listener
    }

    private fun completeMenu() {
        val inflater = LayoutInflater.from(context)

        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val id = item.itemId
            val view = inflater.inflate(R.layout.layout_bottom_item, this, false)
            view.findViewById<ImageView>(R.id.iv_icon).setImageDrawable(item.icon)
            view.findViewById<TextView>(R.id.tv_label).text = item.title
            view.setOnClickListener { onMenuItemClickListener?.invoke(id) }
            view.isEnabled = item.isEnabled
            addView(view)
        }
    }
}