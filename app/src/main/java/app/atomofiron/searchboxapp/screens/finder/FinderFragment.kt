package app.atomofiron.searchboxapp.screens.finder

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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
import app.atomofiron.searchboxapp.databinding.FragmentFinderBinding
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapter
import app.atomofiron.searchboxapp.screens.finder.history.adapter.HistoryAdapter
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

class FinderFragment : Fragment(R.layout.fragment_finder),
    BaseFragment<FinderFragment, FinderViewModel, FinderPresenter> by BaseFragmentImpl()
{

    private lateinit var binding: FragmentFinderBinding
    private val finderAdapter = FinderAdapter()

    private val historyAdapter: HistoryAdapter = HistoryAdapter(object : HistoryAdapter.OnItemClickListener {
        override fun onItemClick(node: String) {
            binding.verticalDock.close()
            presenter.onHistoryItemClick(node)
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, FinderViewModel::class, savedInstanceState)

        finderAdapter.output = presenter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentFinderBinding.bind(view)

        binding.recyclerView.run {
            val linearLayoutManager = LinearLayoutManager(context)
            linearLayoutManager.reverseLayout = true
            layoutManager = linearLayoutManager
            itemAnimator = null
            adapter = finderAdapter
        }

        binding.bottomBar.setOnMenuItemClickListener { id ->
            when (id) {
                R.id.menu_history -> binding.verticalDock.open()
                R.id.menu_explorer -> presenter.onExplorerOptionSelected()
                R.id.menu_options -> presenter.onConfigOptionSelected()
                R.id.menu_settings -> presenter.onSettingsOptionSelected()
            }
        }

        binding.verticalDock.run {
            onGravityChangeListener = presenter::onDockGravityChange
            recyclerView.adapter = historyAdapter
        }

        viewModel.onViewCollect()
        onApplyInsets(view)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (hidden) {
            view?.let {
                val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                inputMethodManager?.hideSoftInputFromWindow(it.windowToken, 0)
            }
        }
    }

    override fun FinderViewModel.onViewCollect() {
        viewCollect(historyDrawerGravity) { binding.verticalDock.gravity = it }
        viewCollect(reloadHistory, collector = historyAdapter::reload)
        viewCollect(history, collector = historyAdapter::add)
        viewCollect(insertInQuery, collector = ::insertInQuery)
        viewCollect(searchItems, collector = ::onStateChange)
        viewCollect(replaceQuery, collector = ::replaceQuery)
        viewCollect(snackbar, collector = ::showSnackbar)
    }

    override fun onApplyInsets(root: View) {
        ViewGroupInsetsProxy.set(root)
        ViewGroupInsetsProxy.set(binding.coordinator)
        ViewGroupInsetsProxy.set(binding.verticalDock)
        ViewInsetsController.bindPadding(binding.recyclerView, start = true, top = true, end = true, bottom = true)
        ViewInsetsController.bindPadding(binding.bottomAppBar, bottom = true)
    }

    override fun onBack(): Boolean {
        val consumed = binding.verticalDock.isOpened
        binding.verticalDock.close()
        return consumed || super.onBack()
    }

    private fun onStateChange(state: List<FinderStateItem>) = finderAdapter.setItems(state)

    private fun replaceQuery(value: String) {
        view?.findViewById<EditText>(R.id.item_find_rt_find)?.setText(value)
    }

    private fun showSnackbar(value: String) {
        val view = view ?: return
        Snackbar.make(view, value, Snackbar.LENGTH_SHORT)
                .setAnchorView(anchorView)
                .show()
    }

    private fun insertInQuery(value: String) {
        view?.findViewById<EditText>(R.id.item_find_rt_find)
                ?.takeIf { it.isFocused }
                ?.apply {
                    text.replace(selectionStart, selectionEnd, value)
                }
    }
}