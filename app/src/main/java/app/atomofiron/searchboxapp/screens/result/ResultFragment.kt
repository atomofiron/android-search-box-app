package app.atomofiron.searchboxapp.screens.result

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.flow.viewCollect
import lib.atomofiron.android_window_insets_compat.ViewGroupInsetsProxy
import lib.atomofiron.android_window_insets_compat.ViewInsetsController
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.anchorView
import app.atomofiron.searchboxapp.databinding.FragmentResultBinding
import app.atomofiron.searchboxapp.model.finder.FinderTask
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.sheet.BottomSheetMenuWithTitle
import app.atomofiron.searchboxapp.screens.result.adapter.ResultAdapter

class ResultFragment : Fragment(R.layout.fragment_result),
    BaseFragment<ResultFragment, ResultViewModel, ResultPresenter> by BaseFragmentImpl()
{

    private lateinit var binding: FragmentResultBinding

    private val resultAdapter = ResultAdapter()
    private lateinit var bottomItemMenu: BottomSheetMenuWithTitle
    private val errorSnackbar by lazy(LazyThreadSafetyMode.NONE) {
        Snackbar.make(requireView(), "", Snackbar.LENGTH_INDEFINITE)
            .setAnchorView(anchorView)
            .setAction(R.string.dismiss) { presenter.onDropTaskErrorClick() }
    }
    private var snackbarError: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, ResultViewModel::class, savedInstanceState)

        resultAdapter.itemActionListener = presenter
        bottomItemMenu = BottomSheetMenuWithTitle(R.menu.item_options_result, requireContext(), presenter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentResultBinding.bind(view)

        binding.recyclerView.run {
            itemAnimator = null
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resultAdapter
        }
        binding.bottomBar.setOnMenuItemClickListener(::onBottomMenuItemClick)
        bottomItemMenu.bottomSheetView = binding.bottomSheet
        viewModel.onViewCollect()
        onApplyInsets(view)
    }

    private fun onBottomMenuItemClick(id: Int) {
        when (id) {
            R.id.menu_stop -> presenter.onStopClick()
            R.id.menu_options -> presenter.onOptionsClick()
            R.id.menu_export -> presenter.onExportClick()
        }
    }

    override fun ResultViewModel.onViewCollect() {
        viewCollect(composition, collector = ::onCompositionChange)
        viewCollect(task, collector = ::onTaskChange)
        viewCollect(enableOptions, collector = ::enableOptions)
        viewCollect(showOptions, collector = ::showOptions)
        viewCollect(notifyTaskHasChanged) { resultAdapter.notifyDataSetChanged() }
        viewCollect(notifyItemChanged, collector = resultAdapter::setItem) // todo works poor
    }

    override fun onApplyInsets(root: View) {
        ViewGroupInsetsProxy.set(root)
        ViewGroupInsetsProxy.set(binding.bottomSheet)
        ViewInsetsController.bindPadding(binding.recyclerView, start = true, top = true, end = true, bottom = true)
        ViewInsetsController.bindPadding(binding.bottomAppBar, bottom = true)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        resultAdapter.notifyItemChanged(0)
    }

    @SuppressLint("RestrictedApi")
    private fun onTaskChange(task: FinderTask) {
        binding.ballsView.isVisible = task.inProgress
        binding.ivStatus.run {
            isGone = task.inProgress
            isActivated = task.isDone
            isEnabled = task.error == null
        }
        binding.tvCounter.text = "${task.results.size}/${task.count}"
        binding.bottomBar.run {
            var item = menu.findItem(R.id.menu_stop)
            if (item.isEnabled != task.inProgress) {
                item.isEnabled = task.inProgress
            }
            item = menu.findItem(R.id.menu_export)
            if (item.isEnabled != task.results.isNotEmpty()) {
                item.isEnabled = task.results.isNotEmpty()
            }
        }
        resultAdapter.setResults(task.results)

        if (task.results.isNotEmpty()) {
            // fix first item offset
            resultAdapter.notifyItemChanged(0)
        }

        if (task.error != snackbarError) {
            errorSnackbar.setText(task.error!!).show()
        }
    }

    private fun onCompositionChange(composition: ExplorerItemComposition) {
        resultAdapter.setComposition(composition)
    }

    @SuppressLint("RestrictedApi")
    private fun enableOptions(enable: Boolean) {
        val item = binding.bottomBar.menu.findItem(R.id.menu_options)
        if (item.isEnabled != enable) {
            item.isEnabled = enable
        }
    }

    private fun showOptions(options: ExplorerItemOptions) {
        bottomItemMenu.show(options)
        if (options.items.size == 1) {
            bottomItemMenu.tvDescription.isVisible = true
            bottomItemMenu.tvDescription.text = options.items[0].completedPath
        } else {
            bottomItemMenu.tvDescription.isGone = true
        }
    }
}