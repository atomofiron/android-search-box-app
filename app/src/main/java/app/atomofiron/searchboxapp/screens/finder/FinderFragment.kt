package app.atomofiron.searchboxapp.screens.finder

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.Knife
import app.atomofiron.common.util.flow.viewCollect
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.anchorView
import app.atomofiron.searchboxapp.custom.view.BottomMenuBar
import app.atomofiron.searchboxapp.custom.view.VerticalDockView
import app.atomofiron.searchboxapp.poop
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapter
import app.atomofiron.searchboxapp.screens.finder.history.adapter.HistoryAdapter
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

class FinderFragment : Fragment(R.layout.fragment_finder),
    BaseFragment<FinderFragment, FinderViewModel, FinderPresenter> by BaseFragmentImpl()
{

    private val rvContent = Knife<RecyclerView>(this, R.id.finder_rv)
    private val bottomOptionMenu = Knife<BottomMenuBar>(this, R.id.finder_bom)
    private val dockView = Knife<VerticalDockView>(this, R.id.finder_dv)

    private val finderAdapter = FinderAdapter()

    private val historyAdapter: HistoryAdapter = HistoryAdapter(object : HistoryAdapter.OnItemClickListener {
        override fun onItemClick(node: String) {
            dockView { close() }
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

        rvContent {
            val linearLayoutManager = LinearLayoutManager(context)
            linearLayoutManager.reverseLayout = true
            layoutManager = linearLayoutManager
            itemAnimator = null
            adapter = finderAdapter
        }

        bottomOptionMenu {
            setOnMenuItemClickListener { id ->
                when (id) {
                    R.id.menu_history -> dockView { open() }
                    R.id.menu_explorer -> presenter.onExplorerOptionSelected()
                    R.id.menu_options -> presenter.onConfigOptionSelected()
                    R.id.menu_settings -> presenter.onSettingsOptionSelected()
                }
            }
        }

        dockView {
            recyclerView.adapter = historyAdapter
            onGravityChangeListener = presenter::onDockGravityChange
        }

        onViewCollect()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dockView {
            recyclerView.adapter = null
        }
        rvContent {
            adapter = null
        }
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

    private fun onViewCollect() = viewModel.apply {
        viewCollect(historyDrawerGravity) { dockView { gravity = it } }
        viewCollect(reloadHistory, collector = historyAdapter::reload)
        viewCollect(history, collector = historyAdapter::add)
        viewCollect(insertInQuery, collector = ::insertInQuery)
        viewCollect(searchItems, collector = ::onStateChange)
        viewCollect(replaceQuery, collector = ::replaceQuery)
        viewCollect(snackbar, collector = ::showSnackbar)
    }

    override fun onBack(): Boolean {
        val consumed = dockView(default = false) {
            isOpened.apply {
                close()
            }
        }
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