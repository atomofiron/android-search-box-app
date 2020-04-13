package ru.atomofiron.regextool.custom.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.bottomappbar.BottomAppBar

class FixedBottomAppBar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : BottomAppBar(context, attrs) {

    override fun getBehavior(): Behavior {
        return FixedHideBottomViewOnScrollBehavior()
    }
}