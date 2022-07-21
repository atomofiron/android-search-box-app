package app.atomofiron.searchboxapp.screens.curtain

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.Insets
import androidx.core.view.*
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.color.MaterialColors
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.arch.TranslucentFragment
import app.atomofiron.common.util.flow.value
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.FragmentCurtainBinding
import app.atomofiron.searchboxapp.screens.curtain.fragment.CurtainContentDelegate
import app.atomofiron.searchboxapp.screens.curtain.fragment.CurtainNode
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainAction
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.curtain.fragment.TransitionAnimator
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainBackground
import lib.atomofiron.android_window_insets_compat.dispatchChildrenWindowInsets
import lib.atomofiron.android_window_insets_compat.insetsProxying
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min

class CurtainFragment : DialogFragment(R.layout.fragment_curtain),
    BaseFragment<CurtainFragment, CurtainViewModel, CurtainPresenter> by BaseFragmentImpl(),
    TranslucentFragment {
    companion object {
        private const val SAVED_STACK = "SAVED_STACK"
    }
    private lateinit var binding: FragmentCurtainBinding
    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var curtainBackground: CurtainBackground
    private val stack: MutableList<CurtainNode> = ArrayList()
    private val insetsCalculator = SheetInsetsCalculator()
    private lateinit var contentDelegate: CurtainContentDelegate
    private lateinit var transitionAnimator: TransitionAnimator
    private var snackbarView = WeakReference<View>(null)

    override val isLightStatusBar: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, CurtainViewModel::class, savedInstanceState)

        when (savedInstanceState) {
            null -> stack.add(CurtainNode(viewModel.initialLayoutId, view = null, isCancelable = true))
            else -> savedInstanceState.getIntArray(SAVED_STACK)?.let { ids ->
                val restored = ids.map { CurtainNode(layoutId = it, view = null, isCancelable = true) }
                stack.addAll(restored)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        curtainBackground = CurtainBackground(requireContext())

        binding = FragmentCurtainBinding.bind(view)
        binding.curtainSheet.clipToOutline = true
        binding.curtainSheet.background = curtainBackground.outline
        binding.root.background = curtainBackground
        binding.root.setOnClickListener {
            tryHide()
        }
        binding.root.addOnLayoutChangeListener { root, left, _, right, _, _, _, _, _ ->
            onRootLayoutChange(root, rootWidth = right - left)
        }

        val layoutParams = binding.curtainSheet.layoutParams as CoordinatorLayout.LayoutParams
        behavior = layoutParams.behavior as BottomSheetBehavior
        behavior.addBottomSheetCallback(BottomSheetCallbackImpl(binding.curtainSheet))
        behavior.state = BottomSheetBehavior.STATE_HIDDEN

        transitionAnimator = TransitionAnimator(binding, ::updateUi)

        binding.root.insetsProxying()
        ViewCompat.setOnApplyWindowInsetsListener(binding.curtainSheet, insetsCalculator)

        onViewCollect()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(SAVED_STACK, stack.map { it.layoutId }.toIntArray())
    }

    override fun onBack(): Boolean {
        when {
            !viewModel.cancelable.value -> Unit
            !contentDelegate.showPrev() -> hide()
        }
        return true
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            presenter.onShown()
        }
    }

    private fun tryHide() {
        if (viewModel.cancelable.value) {
            hide()
        }
    }

    private fun onRootLayoutChange(rootView: View, rootWidth: Int) {
        val maxWidth = resources.getDimensionPixelSize(R.dimen.curtain_max_width)
        val width = min(maxWidth, rootWidth)
        val padding = (rootWidth - width) / 2
        val curtainSheet = binding.curtainSheet
        if (rootView.paddingStart != padding || rootView.paddingEnd != padding) {
            rootView.updatePaddingRelative(start = padding, end = padding)
            curtainSheet.requestLayout()
        }
        if (!transitionAnimator.ignoreLayoutChanges) {
            updateUi()
        }
    }

    private fun onViewCollect() {
        viewCollect(viewModel.adapter, collector = ::onAdapterCollect)
        viewCollect(viewModel.cancelable) { cancelable ->
            behavior.isHideable = cancelable
        }
        viewCollect(viewModel.action, collector = ::onActionCollect)
    }

    private fun onAdapterCollect(adapter: CurtainApi.Adapter<*>) {
        contentDelegate = CurtainContentDelegate(binding, stack, adapter, transitionAnimator, presenter)
        contentDelegate.showLast()
        expand()
    }

    private fun onActionCollect(action: CurtainAction) {
        when (action) {
            is CurtainAction.ShowNext -> contentDelegate.showNext(action.layoutId)
            is CurtainAction.ShowPrev -> contentDelegate.showPrev()
            is CurtainAction.Hide -> hide()
            is CurtainAction.ShowSnackbar -> showSnackbar(action.provider)
        }
    }

    private fun showSnackbar(provider: CurtainApi.SnackbarProvider) {
        val context = context ?: return
        val colorSurface = ContextCompat.getColor(context, R.color.colorSurface)
        val colorOnSurface = ContextCompat.getColor(context, R.color.colorOnSurface)
        val colorPrimary = ContextCompat.getColor(context, R.color.colorPrimaryLight)
        val alpha = ResourcesCompat.getFloat(resources, R.dimen.mtrl_snackbar_background_overlay_color_alpha)
        val backgroundColor = MaterialColors.layer(colorOnSurface, colorSurface, alpha)
        provider.getSnackbar(binding.root)
            .setBackgroundTint(backgroundColor)
            .setTextColor(colorOnSurface)
            .setActionTextColor(colorPrimary)
            .apply {
                view.elevation = binding.curtainSheet.elevation
                snackbarView = WeakReference(view)
                ViewCompat.setOnApplyWindowInsetsListener(view) { _, _ ->
                    WindowInsetsCompat.CONSUMED
                }
            }
            .show()
    }

    private fun updateUi() {
        updateSaturation()
        updateSnackbarTranslation()
        updateCurtainBackground()
    }

    private fun updateSaturation() {
        if (transitionAnimator.ignoreLayoutChanges) {
            return
        }
        val curtainRoot = binding.curtainRoot
        val curtainSheet = binding.curtainSheet
        val peekHeight = when {
            behavior.peekHeight > 0 -> behavior.peekHeight
            else -> curtainSheet.height
        }
        val slidePeekHeight = (curtainRoot.height - curtainSheet.top).toFloat()
        var saturation = max(0f, slidePeekHeight / peekHeight)
        saturation = min(1f, saturation)
        curtainBackground.setSaturation(saturation)
    }

    private fun updateSnackbarTranslation() {
        val curtainRoot = binding.curtainRoot
        val curtainSheet = binding.curtainSheet
        val snackbarView = snackbarView.get() ?: return
        val params = snackbarView.layoutParams as ViewGroup.MarginLayoutParams
        var limit = snackbarView.height + params.topMargin + params.bottomMargin
        limit += insetsCalculator.insetTop
        limit -= curtainRoot.height
        var slidePeekHeight = curtainRoot.height - curtainSheet.top - insetsCalculator.insetTop
        if (slidePeekHeight < snackbarView.height) {
            slidePeekHeight -= snackbarView.height - slidePeekHeight
        }
        snackbarView.translationY = max(limit, -slidePeekHeight).toFloat()
    }

    private fun updateCurtainBackground() {
        curtainBackground.updateTrueBounds(
            insetsCalculator.insetTop,
            binding.curtainSheet.top,
            max(binding.curtainRoot.height, binding.curtainSheet.bottom) - insetsCalculator.insetBottom,
            binding.curtainRoot.paddingStart,
        )
    }

    private fun expand() {
        view?.post {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun hide() {
        behavior.isHideable = true
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private inner class BottomSheetCallbackImpl(bottomSheet: View) : BottomSheetBehavior.BottomSheetCallback() {
        init {
            bottomSheet.post {
                updateSaturation()
            }
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            updateSaturation()
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                presenter.onHidden()
            }
        }

        // slideOffset is broken
        override fun onSlide(bottomSheet: View, slideOffset: Float) = updateUi()
    }

    private class SheetInsetsCalculator : OnApplyWindowInsetsListener {
        var insetTop = 0
            private set
        var insetBottom = 0
            private set
        var paddingTop = 0
            private set

        override fun onApplyWindowInsets(view: View, insets: WindowInsetsCompat): WindowInsetsCompat {
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            var statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            insetBottom = ime.bottom
            insetTop = statusBars.top
            paddingTop = view.resources.getDimensionPixelSize(R.dimen.curtain_padding_top)
            val top = paddingTop + statusBars.top
            statusBars = Insets.of(statusBars.left, top, statusBars.right, statusBars.bottom)
            val new = WindowInsetsCompat.Builder(insets)
                .setInsets(WindowInsetsCompat.Type.statusBars(), statusBars)
                .build()
            (view as ViewGroup).dispatchChildrenWindowInsets(new)
            return WindowInsetsCompat.CONSUMED
        }
    }
}