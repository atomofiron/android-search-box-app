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
import ru.atomofiron.regextool.R
import app.atomofiron.common.base.BaseFragment
import app.atomofiron.common.util.Knife
import ru.atomofiron.regextool.screens.finder.adapter.FinderActionListenerDelegate
import ru.atomofiron.regextool.screens.finder.adapter.FinderAdapter
import ru.atomofiron.regextool.screens.finder.history.adapter.HistoryAdapter
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem
import ru.atomofiron.regextool.screens.finder.model.FinderStateItemUpdate
import ru.atomofiron.regextool.view.custom.BottomOptionMenu
import ru.atomofiron.regextool.view.custom.VerticalDockView
import kotlin.reflect.KClass

class FinderFragment : BaseFragment<FinderViewModel>() {
    override val viewModelClass: KClass<FinderViewModel> = FinderViewModel::class
    override val layoutId: Int = R.layout.fragment_finder

    private val rvContent = Knife<RecyclerView>(this, R.id.finder_rv)
    private val bottomOptionMenu = Knife<BottomOptionMenu>(this, R.id.finder_bom)
    private val dockView = Knife<VerticalDockView>(this, R.id.finder_dv)

    private val finderAdapter = FinderAdapter()

    private val historyAdapter: HistoryAdapter = HistoryAdapter(object : HistoryAdapter.OnItemClickListener {
        override fun onItemClick(node: String?) {
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        finderAdapter.onFinderActionListener = FinderActionListenerDelegate(viewModel, historyAdapter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvContent {
            val linearLayoutManager = LinearLayoutManager(context!!)
            linearLayoutManager.reverseLayout = true
            layoutManager = linearLayoutManager
            itemAnimator = null
            adapter = finderAdapter
        }

        bottomOptionMenu {
            setOnMenuItemClickListener { id ->
                when (id) {
                    R.id.menu_history -> dockView { open() }
                    R.id.menu_explorer -> viewModel.onExplorerOptionSelected()
                    R.id.menu_config -> viewModel.onConfigOptionSelected()
                    R.id.menu_settings -> viewModel.onSettingsOptionSelected()
                }
            }
        }

        dockView {
            recyclerView.adapter = historyAdapter
            onGravityChangeListener = viewModel::onDockGravityChange
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (hidden) {
            view?.let {
                val inputMethodManager = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                inputMethodManager?.hideSoftInputFromWindow(it.windowToken, 0)
            }
        }
    }

    override fun onSubscribeData(owner: LifecycleOwner) {
        viewModel.historyDrawerGravity.observe(owner, Observer { dockView { gravity = it } })
        viewModel.reloadHistory.observeEvent(owner, historyAdapter::reload)
        viewModel.insertInQuery.observeData(owner, ::insertInQuery)
        viewModel.state.observe(owner, Observer(::onStateChange))
        viewModel.updateContent.observeData(owner, ::onContentUpdate)
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

    private fun insertInQuery(value: String) {
        view?.findViewById<EditText>(R.id.item_find_rt_find)
                ?.takeIf { it.isFocused }
                ?.apply {
                    text.replace(selectionStart, selectionEnd, value)
                }
    }
}