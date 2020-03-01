package ru.atomofiron.regextool.screens.finder

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.base.BaseFragment
import ru.atomofiron.regextool.common.util.Knife
import ru.atomofiron.regextool.screens.finder.adapter.FinderActionListenerDelegate
import ru.atomofiron.regextool.screens.finder.adapter.FinderAdapter
import ru.atomofiron.regextool.screens.finder.history.adapter.HistoryAdapter
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem
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
            layoutManager = linearLayoutManager
            linearLayoutManager.reverseLayout = true
            adapter = finderAdapter
        }

        bottomOptionMenu.view.setOnMenuItemClickListener { id ->
            when (id) {
                R.id.menu_history -> dockView { open() }
                R.id.menu_explorer -> viewModel.onExplorerOptionSelected()
                R.id.menu_config -> viewModel.onConfigOptionSelected()
                R.id.menu_settings -> viewModel.onSettingsOptionSelected()
            }
        }

        dockView {
            recyclerView.adapter = historyAdapter
            onGravityChangeListener = viewModel::onDockGravityChange
        }
    }

    override fun onSubscribeData(owner: LifecycleOwner) {
        viewModel.historyDrawerGravity.observe(owner, Observer { dockView { gravity = it } })
        viewModel.reloadHistory.observeEvent(owner, historyAdapter::reload)
        viewModel.insertInQuery.observeData(owner, ::insertInQuery)
        viewModel.state.observe(owner, Observer(::onStateChange))
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

    private fun insertInQuery(value: String) {
        view?.findViewById<EditText>(R.id.item_find_rt_find)?.apply {
            text.replace(selectionStart, selectionEnd, value)
        }
    }
}