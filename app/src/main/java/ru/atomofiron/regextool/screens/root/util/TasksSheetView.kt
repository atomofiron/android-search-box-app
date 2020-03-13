package ru.atomofiron.regextool.screens.root.util

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.view.custom.bottom_sheet.BottomSheetView
import kotlin.math.min

class TasksSheetView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : BottomSheetView(context, attrs, defStyleAttr) {

    private val rvTasks: RecyclerView by lazy { findViewById<RecyclerView>(R.id.tasks_rv) }

    private var actionDownY = -1f
    private var reopen = false

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.layout_tasks, this)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        updateLayout()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        updateLayout()
        super.onLayout(changed, left, top, right, bottom)
    }

    private fun updateLayout() {
        val parentHeight = measuredHeight
        if (parentHeight == 0) {
            return
        }
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val maxHeight = resources.getDimensionPixelSize(R.dimen.bottom_sheet_max_height)
        var height = min(screenWidth, screenHeight)
        height = min(height, maxHeight)
        height = min(height, parentHeight)
        if (contentView.layoutParams.height != height) {
            contentView.layoutParams.height = height
            contentView.layoutParams = contentView.layoutParams
        }
    }

    fun setTrackingView(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    actionDownY = event.y
                    reopen = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dif = actionDownY - event.y
                    when {
                        reopen -> Unit
                        dif > v.height / 3 -> {
                            event.action = MotionEvent.ACTION_DOWN
                            reopen = true
                            show()
                        }
                        dif < 0 -> actionDownY = -1f
                    }
                }
            }
            if (reopen) {
                super.onInterceptTouchEvent(event)
            }
            false
        }
    }
}