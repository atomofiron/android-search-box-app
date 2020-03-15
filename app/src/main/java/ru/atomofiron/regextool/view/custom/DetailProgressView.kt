package ru.atomofiron.regextool.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import app.atomofiron.common.util.lazyView
import app.atomofiron.common.util.moveChildrenFrom
import com.google.android.material.textview.MaterialTextView
import ru.atomofiron.regextool.R

class DetailProgressView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val labelStart: MaterialTextView by lazyView(R.id.progress_mtv_start, this)
    private val labelProgress: MaterialTextView by lazyView(R.id.progress_mtv_progress, this)
    private val labelEnd: MaterialTextView by lazyView(R.id.progress_mtv_end, this)
    private val vStart: View by lazyView(R.id.progress_v_start, this)
    private val vEnd: View by lazyView(R.id.progress_v_end, this)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        moveChildrenFrom(R.layout.view_progress_detail)
    }

    @Suppress("NAME_SHADOWING")
    fun set(max: Long, progress: Long, labelStart: String?, labelProgress: String?, labelEnd: String?) {
        var max = max
        var progress = progress
        while (max > Int.MAX_VALUE) {
            max /= 2
            progress /= 2
        }
        set(max.toFloat(), progress.toFloat(), labelStart, labelProgress, labelEnd)
    }

    fun set(max: Int, progress: Int, labelStart: String?, labelProgress: String?, labelEnd: String?) {
        set(max.toFloat(), progress.toFloat(), labelStart, labelProgress, labelEnd)
    }

    fun set(max: Float, progress: Float, labelStart: String?, labelProgress: String?, labelEnd: String?) {
        require(max >= progress) { IllegalArgumentException("Max value < progress value!") }

        (vStart.layoutParams as LayoutParams).weight = progress
        (vEnd.layoutParams as LayoutParams).weight = max - progress
        vStart.layoutParams = vStart.layoutParams
        vEnd.layoutParams = vEnd.layoutParams
        //vEnd.layoutParams = vEnd.layoutParams

        this.labelStart.text = labelStart
        this.labelProgress.text = labelProgress
        this.labelEnd.text = labelEnd
    }
}