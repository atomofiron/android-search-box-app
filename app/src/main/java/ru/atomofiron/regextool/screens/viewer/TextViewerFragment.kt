package ru.atomofiron.regextool.screens.viewer

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.arch.fragment.BaseFragment
import app.atomofiron.common.util.Knife
import app.atomofiron.common.util.setVisible
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.BallsView
import ru.atomofiron.regextool.custom.view.BottomMenuBar
import ru.atomofiron.regextool.custom.view.FixedBottomAppBar
import ru.atomofiron.regextool.custom.view.bottom_sheet.BottomSheetView
import ru.atomofiron.regextool.model.finder.FinderQueryParams
import ru.atomofiron.regextool.screens.viewer.recycler.TextViewerAdapter
import ru.atomofiron.regextool.screens.viewer.sheet.SearchDelegate
import javax.inject.Inject
import kotlin.reflect.KClass

class TextViewerFragment : BaseFragment<TextViewerViewModel, TextViewerPresenter>() {
    companion object {
        const val KEY_PATH = "KEY_PATH"
        const val KEY_QUERY = "KEY_QUERY"
        const val KEY_USE_SU = "KEY_USE_SU"
        const val KEY_IGNORE_CASE = "KEY_IGNORE_CASE"

        fun openTextFile(path: String, params: FinderQueryParams? = null): Fragment {
            val bundle = Bundle()
            bundle.putString(KEY_PATH, path)
            bundle.putString(KEY_QUERY, params?.query)
            bundle.putBoolean(KEY_USE_SU, params?.useRegex ?: false)
            bundle.putBoolean(KEY_IGNORE_CASE, params?.ignoreCase ?: false)
            val fragment = TextViewerFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
    override val viewModelClass: KClass<TextViewerViewModel> = TextViewerViewModel::class
    override val layoutId: Int = R.layout.fragment_text_viewer

    private val bottomSheetView = Knife<BottomSheetView>(this, R.id.text_viewer_bsv)
    private val rvTextViewer = Knife<RecyclerView>(this, R.id.text_viewer_rv)
    private val tvCounter = Knife<TextView>(this, R.id.text_viewer_tv_counter)
    private val bvLoading = Knife<BallsView>(this, R.id.text_viewer_bv)
    private val bottomMenuBar = Knife<BottomMenuBar>(this, R.id.text_viewer_bmb)
    private val bottomAppBar = Knife<FixedBottomAppBar>(this, R.id.text_viewer_fbab)

    @Inject
    override lateinit var presenter: TextViewerPresenter

    private val viewerAdapter = TextViewerAdapter()

    private lateinit var searchDelegate: SearchDelegate

    override fun inject() = viewModel.inject(this)

    override fun onCreate() {
        viewerAdapter.textViewerListener = presenter
        searchDelegate = SearchDelegate(presenter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvTextViewer {
            layoutManager = LinearLayoutManager(context)
            adapter = viewerAdapter
            itemAnimator = null
        }
        bottomMenuBar {
            setOnMenuItemClickListener { id ->
                when (id) {
                    R.id.menu_search -> searchDelegate.show(viewModel.xFile, viewModel.composition)
                    R.id.menu_previous -> presenter.onPreviousClick()
                    R.id.menu_next -> presenter.onNextClick()
                }
            }
        }
        searchDelegate.bottomSheetView = bottomSheetView.view
    }

    override fun onStart() {
        super.onStart()
        bottomAppBar {
            updateElevation()
        }
    }

    override fun onSubscribeData(owner: LifecycleOwner) {
        super.onSubscribeData(owner)

        viewModel.loading.observe(owner, Observer(::setLoading))
        viewModel.textLines.observe(owner, Observer(viewerAdapter::setItems))
        viewModel.matchesMap.observe(owner, Observer(viewerAdapter::setMatches))
        viewModel.matchesCursor.observe(owner, Observer(::onMatchCursorChanged))
        viewModel.matchesCounter.observe(owner, Observer(::onMatchCounterChanged))
        viewModel.searchItems.observe(owner, Observer(searchDelegate::setItems))
        viewModel.insertInQuery.observeData(owner, ::insertInQuery)
    }

    override fun onBack(): Boolean = bottomSheetView.view.hide() || super.onBack()

    private fun setLoading(visible: Boolean) {
        bvLoading {
            setVisible(visible, invisibleMode = View.INVISIBLE)
        }
        onMatchCounterChanged(viewModel.matchesCounter.value)
    }

    private fun onMatchCounterChanged(counter: Long?) {
        var index: Int? = null
        var count: Int? = null
        tvCounter {
            text = when (counter) {
                null -> null
                else -> {
                    index = counter.shr(32).toInt()
                    count = counter.toInt()
                    "$index / $count"
                }
            }
        }
        bottomMenuBar {
            val loading = viewModel.loading.value
            menu.findItem(R.id.menu_previous).isEnabled = !loading && index != null && index!! > 1
            menu.findItem(R.id.menu_next).isEnabled = !loading && count != null && index != count
        }
    }

    private fun onMatchCursorChanged(cursor: Long?) {
        viewerAdapter.setCursor(cursor)
    }

    private fun insertInQuery(value: String) {
        view?.findViewById<EditText>(R.id.item_find_rt_find)
                ?.takeIf { it.isFocused }
                ?.apply {
                    text.replace(selectionStart, selectionEnd, value)
                }
    }
}