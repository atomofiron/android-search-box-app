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
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.anchorView
import app.atomofiron.searchboxapp.databinding.FragmentFinderBinding
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapter
import app.atomofiron.searchboxapp.screens.finder.history.adapter.HistoryAdapter
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import app.atomofiron.searchboxapp.setContentMaxWidthRes
import lib.atomofiron.android_window_insets_compat.applyPaddingInsets
import lib.atomofiron.android_window_insets_compat.insetsProxying

class FinderFragment : Fragment(R.layout.fragment_finder),
    BaseFragment<FinderFragment, FinderViewState, FinderPresenter> by BaseFragmentImpl()
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

        binding.bottomBar.setContentMaxWidthRes(R.dimen.bottom_bar_max_width)
        binding.bottomBar.isItemActiveIndicatorEnabled = false
        binding.bottomBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_history -> binding.verticalDock.open()
                R.id.menu_explorer -> presenter.onExplorerOptionSelected()
                R.id.menu_options -> presenter.onConfigOptionSelected()
                R.id.menu_settings -> presenter.onSettingsOptionSelected()
            }
            false
        }

        binding.verticalDock.run {
            onGravityChangeListener = presenter::onDockGravityChange
            recyclerView.adapter = historyAdapter
        }

        viewState.onViewCollect()
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

    override fun FinderViewState.onViewCollect() {
        viewCollect(historyDrawerGravity) { binding.verticalDock.gravity = it }
        viewCollect(reloadHistory, collector = historyAdapter::reload)
        viewCollect(history, collector = historyAdapter::add)
        viewCollect(insertInQuery, collector = ::onInsertInQuery)
        viewCollect(searchItems, collector = ::onStateChange)
        viewCollect(replaceQuery, collector = ::onReplaceQuery)
        viewCollect(snackbar, collector = ::onShowSnackbar)
    }

    override fun onApplyInsets(root: View) {
        root.insetsProxying()
        binding.coordinator.insetsProxying()
        binding.recyclerView.applyPaddingInsets()
        binding.bottomBar.applyPaddingInsets(bottom = true)
    }

    override fun onBack(): Boolean {
        val consumed = binding.verticalDock.isOpened
        binding.verticalDock.close()
        return consumed || super.onBack()
    }

    private fun onStateChange(state: List<FinderStateItem>) = finderAdapter.setItems(state)

    private fun onReplaceQuery(value: String) {
        view?.findViewById<EditText>(R.id.item_find_rt_find)?.setText(value)
    }

    private fun onShowSnackbar(value: String) {
        val view = view ?: return
        Snackbar.make(view, value, Snackbar.LENGTH_SHORT)
                .setAnchorView(anchorView)
                .show()
    }

    private fun onInsertInQuery(value: String) {
        view?.findViewById<EditText>(R.id.item_find_rt_find)
                ?.takeIf { it.isFocused }
                ?.apply {
                    text.replace(selectionStart, selectionEnd, value)
                }
    }
}