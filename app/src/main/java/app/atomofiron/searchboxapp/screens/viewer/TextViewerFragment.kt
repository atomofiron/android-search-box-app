package app.atomofiron.searchboxapp.screens.viewer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.Knife
import app.atomofiron.common.util.flow.value
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.common.util.setVisible
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.BallsView
import app.atomofiron.searchboxapp.custom.view.BottomMenuBar
import app.atomofiron.searchboxapp.custom.view.FixedBottomAppBar
import app.atomofiron.searchboxapp.custom.view.bottom_sheet.BottomSheetView
import app.atomofiron.searchboxapp.model.finder.FinderQueryParams
import app.atomofiron.searchboxapp.screens.viewer.recycler.TextViewerAdapter
import app.atomofiron.searchboxapp.screens.viewer.sheet.SearchDelegate

class TextViewerFragment : Fragment(R.layout.fragment_text_viewer),
    BaseFragment<TextViewerFragment, TextViewerViewModel, TextViewerPresenter> by BaseFragmentImpl()
{
    companion object {
        const val KEY_PATH = "KEY_PATH"
        const val KEY_QUERY = "KEY_QUERY"
        const val KEY_USE_REGEX = "KEY_USE_REGEX"
        const val KEY_IGNORE_CASE = "KEY_IGNORE_CASE"
    }

    private val bottomSheetView = Knife<BottomSheetView>(this, R.id.text_viewer_bsv)
    private val rvTextViewer = Knife<RecyclerView>(this, R.id.text_viewer_rv)
    private val tvCounter = Knife<TextView>(this, R.id.text_viewer_tv_counter)
    private val bvLoading = Knife<BallsView>(this, R.id.text_viewer_bv)
    private val bottomMenuBar = Knife<BottomMenuBar>(this, R.id.text_viewer_bmb)
    private val bottomAppBar = Knife<FixedBottomAppBar>(this, R.id.text_viewer_fbab)

    private val viewerAdapter = TextViewerAdapter()

    private lateinit var searchDelegate: SearchDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, TextViewerViewModel::class, savedInstanceState)
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
        onViewCollect()
    }

    override fun onStart() {
        super.onStart()
        bottomAppBar {
            updateElevation()
        }
    }

    private fun onViewCollect() = viewModel.apply {
        viewCollect(loading, collector = ::setLoading)
        viewCollect(textLines, collector = viewerAdapter::setItems)
        viewCollect(matchesMap, collector = viewerAdapter::setMatches)
        viewCollect(matchesCursor, collector = ::onMatchCursorChanged)
        viewCollect(matchesCounter, collector = ::onMatchCounterChanged)
        viewCollect(searchItems, collector = searchDelegate::setItems)
        viewCollect(insertInQuery, collector = ::insertInQuery)
        viewCollect(closeBottomSheet, collector = this@TextViewerFragment::closeBottomSheet)
    }

    override fun onBack(): Boolean = bottomSheetView.view.hide() || super.onBack()

    private fun setLoading(visible: Boolean) {
        bvLoading {
            setVisible(visible, invisibleMode = View.INVISIBLE)
        }
        onMatchCounterChanged(viewModel.matchesCounter.value)
    }

    @SuppressLint("RestrictedApi")
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

    private fun closeBottomSheet(unit: Unit = Unit) {
        bottomSheetView {
            hide()
        }
    }
}