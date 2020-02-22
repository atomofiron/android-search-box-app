package ru.atomofiron.regextool.view.custom.bottom_sheet

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MenuInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R

class BottomSheetMenu @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : BottomSheetView(context, attrs, defStyleAttr), ValueAnimator.AnimatorUpdateListener {

    init {
        val styled = context.obtainStyledAttributes(attrs, R.styleable.BottomSheetMenu, defStyleAttr, 0)
        val menuId = styled.getResourceId(R.styleable.BottomSheetMenu_menu, 0)
        styled.recycle()

        if (menuId != 0) {
            inflateMenu(menuId)
        }
    }

    fun inflateMenu(menuId: Int) {
        val rvMenu = RecyclerView(context)
        rvMenu.layoutManager = LinearLayoutManager(context)
        rvMenu.adapter = BottomSheetViewAdapter(context).apply {
            MenuInflater(context).inflate(menuId, menu)
        }
        rvMenu.clipToPadding = false
        setView(rvMenu)
    }
}