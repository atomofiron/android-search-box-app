package ru.atomofiron.regextool.view.custom

import android.content.Context
import android.util.AttributeSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomappbar.BottomAppBar

class FixedBottomAppBar : BottomAppBar {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    // todo FUCK com.google.android.material:material:1.1.0 BottomAppBar.Behavior
    override fun getBehavior(): CoordinatorLayout.Behavior<BottomAppBar> = FixedHideBottomViewOnScrollBehavior()
}