package ru.atomofiron.regextool.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.AppCompatImageView
import ru.atomofiron.regextool.R

class ProgressSpinner : AppCompatImageView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setImageResource(R.drawable.ic_progress)
        startAnimation(AnimationUtils.loadAnimation(context!!, R.anim.rotate))
    }
}