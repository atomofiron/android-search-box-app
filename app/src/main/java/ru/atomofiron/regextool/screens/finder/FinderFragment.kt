package ru.atomofiron.regextool.screens.finder

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.arch.fragment.BaseFragment
import app.atomofiron.common.util.Knife
import com.google.android.material.snackbar.Snackbar
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.BottomMenuBar
import ru.atomofiron.regextool.custom.view.VerticalDockView
import ru.atomofiron.regextool.screens.finder.adapter.FinderAdapter
import ru.atomofiron.regextool.screens.finder.history.adapter.HistoryAdapter
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem
import ru.atomofiron.regextool.screens.finder.model.FinderStateItemUpdate
import javax.inject.Inject
import kotlin.reflect.KClass

class FinderFragment : BaseFragment<FinderViewModel, FinderPresenter>() {
    override val viewModelClass: KClass<FinderViewModel> = FinderViewModel::class
    override val layoutId: Int = R.layout.fragment_finder

    @Inject
    override lateinit var presenter: FinderPresenter

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

    override fun inject() = viewModel.inject(this)

    override fun onCreate() {
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
                val inputMethodManager = thisContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                inputMethodManager?.hideSoftInputFromWindow(it.windowToken, 0)
            }
        }
    }

    override fun onSubscribeData(owner: LifecycleOwner) {
        viewModel.historyDrawerGravity.observe(owner, Observer { dockView { gravity = it } })
        viewModel.reloadHistory.observeEvent(owner, historyAdapter::reload)
        viewModel.history.observeData(owner, historyAdapter::add)
        viewModel.insertInQuery.observeData(owner, ::insertInQuery)
        viewModel.state.observe(owner, Observer(::onStateChange))
        viewModel.replaceQuery.observeData(owner, ::replaceQuery)
        viewModel.snackbar.observeData(owner, ::showSnackbar)
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

    private fun onContentUpdate(event: FinderStateItemUpdate) {
        when (event) {
            is FinderStateItemUpdate.Changed -> finderAdapter.setItem(event.index, event.item)
            is FinderStateItemUpdate.Inserted -> finderAdapter.insertItem(event.index, event.item)
            is FinderStateItemUpdate.Removed -> finderAdapter.removeItem(event.index)
        }
    }

    private fun replaceQuery(value: String) {
        view?.findViewById<EditText>(R.id.item_find_rt_find)?.setText(value)
    }

    private fun showSnackbar(value: String) {
        Snackbar.make(thisView, value, Snackbar.LENGTH_SHORT)
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