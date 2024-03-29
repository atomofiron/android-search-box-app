package app.atomofiron.searchboxapp.screens.curtain

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.Insets
import androidx.core.view.*
import androidx.core.view.WindowInsetsCompat.Type
import androidx.fragment.app.DialogFragment
import app.atomofiron.common.arch.*
import app.atomofiron.common.util.findColorByAttr
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.color.MaterialColors
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.common.util.isDarkTheme
import app.atomofiron.searchboxapp.BuildConfig
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.LayoutDelegate.Companion.getLayout
import app.atomofiron.searchboxapp.custom.LayoutDelegate.Companion.setLayoutListener
import app.atomofiron.searchboxapp.databinding.FragmentCurtainBinding
import app.atomofiron.searchboxapp.utils.getColorByAttr
import app.atomofiron.searchboxapp.screens.curtain.fragment.CurtainContentDelegate
import app.atomofiron.searchboxapp.screens.curtain.fragment.CurtainNode
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainAction
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.curtain.fragment.TransitionAnimator
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainBackground
import com.google.android.material.snackbar.Snackbar
import lib.atomofiron.android_window_insets_compat.defaultTypeMask
import lib.atomofiron.android_window_insets_compat.dispatchChildrenWindowInsets
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min

class CurtainFragment : DialogFragment(R.layout.fragment_curtain),
    BaseFragment<CurtainFragment, CurtainViewState, CurtainPresenter> by BaseFragmentImpl(),
    TranslucentFragment {
    companion object {
        private const val SAVED_STACK = "SAVED_STACK"
        private const val MAX_OVERLAY_SATURATION = 200
    }
    private lateinit var binding: FragmentCurtainBinding
    private lateinit var behavior: BottomSheetBehavior<View>
    private val stack: MutableList<CurtainNode> = ArrayList()
    private lateinit var contentDelegate: CurtainContentDelegate
    private lateinit var transitionAnimator: TransitionAnimator
    private var snackbarView = WeakReference<View>(null)
    private var overlayColor = 0

    override val isLightStatusBar: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, CurtainViewModel::class, savedInstanceState)
        overlayColor = requireContext().getColorByAttr(R.attr.colorOverlay)

        when (savedInstanceState) {
            null -> stack.add(CurtainNode(viewState.initialLayoutId, view = null, isCancelable = true))
            else -> savedInstanceState.getIntArray(SAVED_STACK)?.let { ids ->
                val restored = ids.map { CurtainNode(layoutId = it, view = null, isCancelable = true) }
                stack.addAll(restored)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentCurtainBinding.bind(view).apply {
            curtainSheet.clipToOutline = true
            curtainSheet.background = CurtainBackground(requireContext())
            curtainSheet.setOnClickListener { /* intercept clicks */ }
            root.setOnClickListener {
                root.setOnClickListener(null)
                root.setOnLongClickListener(null)
                root.isClickable = false
                root.isLongClickable = false
                tryHide()
            }
            root.setOnLongClickListener {
                if (BuildConfig.DEBUG) showTestSnackbar()
                true
            }
            root.isHapticFeedbackEnabled = false
            root.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateSnackbarTranslation() }
            curtainParent.addOnLayoutChangeListener { parent, left, _, right, _, _, _, _, _ ->
                onCurtainParentChange(parent, rootWidth = right - left)
            }
            val layoutParams = curtainSheet.layoutParams as CoordinatorLayout.LayoutParams
            behavior = layoutParams.behavior as BottomSheetBehavior
            behavior.addBottomSheetCallback(BottomSheetCallbackImpl(curtainSheet))
            behavior.state = BottomSheetBehavior.STATE_HIDDEN

            onApplyInsets()
        }
        transitionAnimator = TransitionAnimator(binding, ::updateSnackbarTranslation)
        viewState.onViewCollect()
    }

    private fun showTestSnackbar() {
        showSnackbar {
            Snackbar.make(it, "Test", Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {}
        }
    }

    private fun FragmentCurtainBinding.onApplyInsets() {
        val joystickSize = resources.getDimensionPixelSize(R.dimen.joystick_size)
        val verticalPadding = resources.getDimensionPixelSize(R.dimen.curtain_padding_top)
        root.setLayoutListener { root.requestApplyInsets() }
        ViewCompat.setOnApplyWindowInsetsListener(root) { root, windowInsets ->
            val insets = windowInsets.getInsets(defaultTypeMask)
            val builder = WindowInsetsCompat.Builder()
            var navigationBars = windowInsets.getInsets(Type.navigationBars())
            val layout = root.getLayout()
            val ime = windowInsets.getInsets(Type.ime())
            val bottomPadding = when {
                ime.bottom > 0 && layout.withJoystick && layout.isBottom -> joystickSize
                ime.bottom > 0 -> 0
                !layout.withJoystick -> verticalPadding
                !layout.isBottom -> verticalPadding
                else -> joystickSize
            }
            navigationBars = Insets.of(0, 0, 0, navigationBars.bottom + bottomPadding)
            builder.setInsets(Type.navigationBars(), navigationBars)
            builder.setInsets(Type.ime(), Insets.of(0, 0, 0, ime.bottom + bottomPadding))
            builder.setInsets(Type.statusBars(), Insets.of(0, verticalPadding, 0, 0))
            val sheetInsets = builder.build()
            curtainSheet.dispatchChildrenWindowInsets(sheetInsets)
            val horizontal = when {
                !layout.withJoystick -> 0 to 0
                layout.isLeft -> joystickSize to 0
                layout.isRight -> 0 to joystickSize
                else -> 0 to 0
            }
            // don not consume ime to let scrolling to text field on keyboard show
            root.updatePadding(
                left = insets.left + horizontal.first,
                top = insets.top,
                right = insets.right + horizontal.second,
                bottom = 0,
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(SAVED_STACK, stack.map { it.layoutId }.toIntArray())
    }

    override fun onBack(): Boolean {
        when {
            transitionAnimator.transitionIsRunning -> Unit
            !viewState.cancelable.value -> Unit
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
        if (viewState.cancelable.value) {
            hide()
        }
    }

    private fun onCurtainParentChange(parentView: View, rootWidth: Int) {
        val maxWidth = resources.getDimensionPixelSize(R.dimen.curtain_max_width)
        val width = min(maxWidth, rootWidth)
        val padding = (rootWidth - width) / 2
        val curtainSheet = binding.curtainSheet
        if (parentView.paddingStart != padding || parentView.paddingEnd != padding) {
            parentView.updatePaddingRelative(start = padding, end = padding)
            curtainSheet.requestLayout()
        }
    }

    override fun CurtainViewState.onViewCollect() {
        viewCollect(adapter, collector = ::onAdapterCollect)
        viewCollect(cancelable, collector = behavior::setHideable)
        viewCollect(action, collector = ::onActionCollect)
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
        val snackbar = provider.getSnackbar(binding.root)
        if (!context.isDarkTheme()) {
            val colorSurface = context.findColorByAttr(R.attr.colorSurface)
            val colorOnSurface = context.findColorByAttr(R.attr.colorOnSurface)
            val colorPrimaryDark = context.findColorByAttr(R.attr.colorPrimaryDark)
            val alpha = ResourcesCompat.getFloat(resources, R.dimen.mtrl_snackbar_background_overlay_color_alpha)
            val backgroundColor = MaterialColors.layer(colorOnSurface, colorSurface, alpha)
            snackbar
                .setBackgroundTint(backgroundColor)
                .setTextColor(colorOnSurface)
                .setActionTextColor(colorPrimaryDark)
        }
        snackbarView = WeakReference(snackbar.view)
        snackbar.view.doOnNextLayout { updateSnackbarTranslation() }
        snackbar.show()
    }

    private fun updateSaturation() {
        val sheet = binding.curtainSheet
        val parent = sheet.parent as View
        val alpha = (1f - (sheet.bottom - parent.height) / sheet.height.toFloat()).coerceIn(0f, 1f)
        val overlayAlpha = (MAX_OVERLAY_SATURATION * alpha).toInt()
        val overlayColor = ColorUtils.setAlphaComponent(overlayColor, overlayAlpha)
        binding.root.setBackgroundColor(overlayColor)
        snackbarView.get()?.alpha = alpha
    }

    private fun updateSnackbarTranslation() {
        val snackbarView = snackbarView.get() ?: return
        val params = snackbarView.layoutParams as ViewGroup.MarginLayoutParams
        val minBottom = binding.root.paddingTop + params.topMargin + snackbarView.height
        val minOffset = minBottom - binding.root.height
        val bottomInset = params.bottomMargin - params.topMargin
        val offset = binding.curtainSheet.top - binding.curtainSheet.height + bottomInset
        snackbarView.translationY = max(minOffset, offset).toFloat()
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
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            updateSaturation()
            updateSnackbarTranslation()
        }
    }
}