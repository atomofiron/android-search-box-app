package app.atomofiron.searchboxapp.screens.root

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.LayoutDelegate
import app.atomofiron.searchboxapp.databinding.FragmentRootBinding
import app.atomofiron.searchboxapp.screens.main.fragment.SnackbarCallbackFragmentDelegate
import app.atomofiron.searchboxapp.screens.main.fragment.SnackbarCallbackFragmentDelegate.SnackbarCallbackOutput
import app.atomofiron.searchboxapp.screens.main.util.SnackbarWrapper
import com.google.android.material.snackbar.Snackbar
import lib.atomofiron.android_window_insets_compat.applyPaddingInsets
import lib.atomofiron.android_window_insets_compat.insetsProxying

class RootFragment : Fragment(R.layout.fragment_root),
    BaseFragment<RootFragment, RootViewState, RootPresenter> by BaseFragmentImpl()
{

    private lateinit var sbExit: SnackbarWrapper
    private var sbListener: SnackbarCallbackOutput? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, RootViewModel::class, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentRootBinding.bind(view)
        sbExit = SnackbarWrapper(requireContext()) {
            Snackbar.make(binding.snackbarContainer, R.string.click_back_to_exit, Snackbar.LENGTH_SHORT)
                .setAction(R.string.exit) { presenter.onExitClick() }
                .apply { sbListener?.let { addCallback(SnackbarCallbackFragmentDelegate(it)) } }
        }
        viewState.showExitSnackbar.collect(lifecycleScope) { listener ->
            sbListener = listener
            sbExit.show()
        }
        binding.applyInsets()
        presenter.onChildrenCreated()
    }

    private fun FragmentRootBinding.applyInsets() {
        val delegate = LayoutDelegate(root, snackbarContainer = snackbarContainer)
        root.insetsProxying { view, insets ->
            delegate.onApplyWindowInsets(view, insets)
        }
        rootFlContainer.insetsProxying()
        snackbarContainer.applyPaddingInsets()
    }

    override fun onBack(): Boolean = presenter.onBack()
}