package app.atomofiron.searchboxapp.screens.curtain.fragment

import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.databinding.FragmentCurtainBinding
import app.atomofiron.searchboxapp.screens.curtain.CurtainPresenter
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import lib.atomofiron.android_window_insets_compat.insetsProxying

class CurtainContentDelegate(
    private val binding: FragmentCurtainBinding,
    private val stack: MutableList<CurtainNode>,
    private val adapter: CurtainApi.Adapter<*>,
    private val transitionAnimator: TransitionAnimator,
    private val presenter: CurtainPresenter,
) {
    fun showLast() {
        val node = stack.last()
        if (node.view == null) {
            val holder = getHolder(node.layoutId)
            holder ?: return
            node.view = holder.view
            node.isCancelable = holder.isCancelable
            node.removeParent()
            binding.curtainSheet.removeAllViews()
            binding.curtainSheet.addView(holder.view)
            binding.curtainSheet.requestApplyInsets()
        }
    }

    fun showNext(layoutId: Int) {
        val holder = getHolder(layoutId)
        holder ?: return
        if (transitionAnimator.transitionIsRunning) return

        val view = holder.view.makeScrollable()
        val node = CurtainNode(layoutId, view, holder.isCancelable)
        stack.add(node)
        node.removeParent()
        binding.curtainSheet.addView(view)
        view.requestApplyInsets()
        transitionAnimator.startTransition(forward = true)
    }

    fun showPrev(): Boolean {
        if (stack.size < 2) return false
        if (transitionAnimator.transitionIsRunning) return false

        val last = stack.removeLast()
        adapter.drop(last.layoutId)

        val prev = stack.last()
        if (prev.view == null) {
            val holder = getHolder(prev.layoutId)
            holder ?: return false
            prev.view = holder.view
            prev.isCancelable = holder.isCancelable
        } else {
            presenter.setCancelable(prev.isCancelable)
        }
        binding.curtainSheet.addView(prev.view, 0)
        transitionAnimator.startTransition(forward = false)
        return true
    }

    private fun getHolder(layoutId: Int): CurtainApi.ViewHolder? {
        val holder = adapter.getViewHolder(binding.root.context, layoutId)
        when (holder) {
            null -> presenter.onNullViewGot()
            else -> presenter.setCancelable(holder.isCancelable)
        }
        return holder
    }

    // make the large content scrollable
    private fun View.makeScrollable(): View {
        val scrollView = when (this) {
            is NestedScrollView -> this
            is RecyclerView -> this
            else -> NestedScrollView(context).apply {
                this@makeScrollable.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                addView(this@makeScrollable)
                insetsProxying()
            }
        }
        // WRAP_CONTENT is necessary to the horizontal transitions in curtain
        scrollView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        return scrollView
    }
}