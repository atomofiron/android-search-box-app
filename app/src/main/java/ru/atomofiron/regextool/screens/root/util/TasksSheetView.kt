package ru.atomofiron.regextool.screens.root.util

import android.animation.ValueAnimator
import android.content.Context
import android.os.Debug
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.getAnimatorDurationScale
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.view.custom.bottom_sheet.BottomSheetView
import kotlin.math.min

class TasksSheetView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : BottomSheetView(context, attrs, defStyleAttr) {

    private val rvTasks: RecyclerView get() = findViewById(R.id.tasks_rv)
    private val pbRam: ProgressBar get() = findViewById(R.id.tasks_pb_ram)
    private val mtvRam: TextView get() = findViewById(R.id.tasks_mtv_ram)

    private var actionDownY = -1f
    private var reopen = false

    private val memoryLoop = ValueAnimator.ofInt(0, 1, 2)

    init {
        setView(R.layout.layout_tasks)

        memoryLoop.duration = (2000 / context.getAnimatorDurationScale()).toLong()
        memoryLoop.repeatMode = ValueAnimator.RESTART
        memoryLoop.repeatCount = ValueAnimator.INFINITE
        memoryLoop.addUpdateListener(StatsUpdater())
        memoryLoop.start()
    }

    fun resetContentView() {
        removeView(contentView)
        setView(R.layout.layout_tasks)
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

    inner class StatsUpdater : ValueAnimator.AnimatorUpdateListener {
        private var lastValue = -1

        override fun onAnimationUpdate(animator: ValueAnimator) {
            val value = animator.animatedValue as Int
            if (value != lastValue) {
                var allocated = Debug.getNativeHeapAllocatedSize()
                allocated += Runtime.getRuntime().totalMemory()
                allocated -= Runtime.getRuntime().freeMemory()
                allocated = Math.round((allocated / 1024).toFloat() / 1024).toLong()
                var heap = Debug.getNativeHeapSize() + Runtime.getRuntime().totalMemory()
                heap = Math.round((heap / 1024).toFloat() / 1024).toLong()
                pbRam.max = heap.toInt()
                pbRam.progress = allocated.toInt()
                mtvRam.text = "%dM".format(allocated)
            }
            lastValue = value
        }
    }
}