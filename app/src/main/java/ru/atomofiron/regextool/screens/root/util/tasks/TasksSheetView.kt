package ru.atomofiron.regextool.screens.root.util.tasks

import android.animation.ValueAnimator
import android.content.Context
import android.os.Debug
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
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
    private var intercept = false

    private val memoryLoop = ValueAnimator.ofInt(0, 1, 2)

    private val adapter = TasksAdapter()
    private val statsUpdater = StatsUpdater()

    init {
        setView(R.layout.sheet_tasks)
        initRecyclerView(rvTasks)

        memoryLoop.duration = (2000 / context.getAnimatorDurationScale()).toLong()
        memoryLoop.repeatMode = ValueAnimator.RESTART
        memoryLoop.repeatCount = ValueAnimator.INFINITE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        memoryLoop.addUpdateListener(statsUpdater)
        memoryLoop.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        memoryLoop.removeUpdateListener(statsUpdater)
        memoryLoop.pause()
    }

    fun resetContentView() {
        removeView(contentView)
        setView(R.layout.sheet_tasks)
        initRecyclerView(rvTasks)
    }

    fun setItems(items: List<XTask>) = adapter.setItems(items)

    private fun initRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.setPadding(recyclerView.paddingLeft, recyclerView.paddingTop, recyclerView.paddingRight, contentView.paddingBottom)
        contentView.setPadding(contentView.paddingLeft, contentView.paddingTop, contentView.paddingRight, 0)
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
                    intercept = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dif = actionDownY - event.y
                    when {
                        intercept -> Unit
                        dif > v.height / 3 -> {
                            event.action = MotionEvent.ACTION_DOWN
                            intercept = true
                            show()
                        }
                        dif < 0 -> actionDownY = -1f
                    }
                }
            }
            if (intercept) {
                super.onInterceptTouchEvent(event)
            }
            false
        }
    }

    inner class StatsUpdater : ValueAnimator.AnimatorUpdateListener {
        private var lastValue = -1

        override fun onAnimationUpdate(animator: ValueAnimator) {
            val value = animator.animatedValue as Int
            when {
                state != State.OPENED -> return
                value != lastValue -> {
                    var allocated = Debug.getNativeHeapAllocatedSize()
                    allocated += Runtime.getRuntime().totalMemory()
                    allocated -= Runtime.getRuntime().freeMemory()
                    allocated = Math.round((allocated / 1024).toFloat() / 1024).toLong()
                    var heap = Debug.getNativeHeapSize() + Runtime.getRuntime().totalMemory()
                    heap = Math.round((heap / 1024).toFloat() / 1024).toLong()
                    pbRam.max = heap.toInt()
                    pbRam.progress = allocated.toInt()
                    val text = "%dM".format(allocated)
                    mtvRam.text = text
                }
            }
            lastValue = value
        }
    }
}