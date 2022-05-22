package app.atomofiron.searchboxapp.screens.curtain.fragment

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import app.atomofiron.searchboxapp.databinding.FragmentCurtainBinding
import app.atomofiron.searchboxapp.utils.Const

class TransitionAnimator(
    private val binding: FragmentCurtainBinding,
    private val transitionCallback: () -> Unit,
) : View.OnLayoutChangeListener, ValueAnimator.AnimatorUpdateListener {
    var ignoreLayoutChanges = false
        private set

    private var forward = true
    private val start = 0f
    private val end = 1f
    private var from = 0
    private var to = 0
    private lateinit var left: View
    private lateinit var right: View

    private val animator = ValueAnimator.ofFloat(start, end)

    init {
        animator.interpolator = DecelerateInterpolator()
        animator.duration = Const.COMMON_DURATION
    }

    fun startTransition(forward: Boolean) {
        this.forward = forward
        ignoreLayoutChanges = true
        binding.curtainSheet.addOnLayoutChangeListener(this)
    }

    override fun onLayoutChange(view: View, l: Int, t: Int, r: Int, b: Int, oL: Int, oT: Int, oR: Int, oB: Int) {
        view.removeOnLayoutChangeListener(this)
        ignoreLayoutChanges = false
        left = binding.curtainSheet.getChildAt(0) ?: return
        right = binding.curtainSheet.getChildAt(1) ?: return
        ignoreLayoutChanges = true
        from = if (forward) left.height else right.height
        to = if (forward) right.height else left.height
        animator.addUpdateListener(this)
        animator.start()
    }

    override fun onAnimationUpdate(animator: ValueAnimator) {
        val value = animator.animatedValue as Float
        val top = binding.root.height - from - (to - from) * value
        binding.curtainSheet.top = top.toInt()
        val width = binding.curtainSheet.width
        val center = when {
            forward -> (width - width * value).toInt()
            else -> (width * value).toInt()
        }
        left.left = center - width
        left.right = center
        right.left = center
        right.right = center + width
        transitionCallback()
        if (value == end) {
            ignoreLayoutChanges = false
            this.animator.removeUpdateListener(this)
            when {
                forward -> binding.curtainSheet.removeView(left)
                else -> binding.curtainSheet.removeView(right)
            }
        }
    }
}