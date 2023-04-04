package app.atomofiron.searchboxapp.screens.viewer

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.OrientationLayoutDelegate
import app.atomofiron.searchboxapp.databinding.FragmentTextViewerBinding
import app.atomofiron.searchboxapp.model.textviewer.SearchTask
import app.atomofiron.searchboxapp.screens.viewer.recycler.TextViewerAdapter
import app.atomofiron.searchboxapp.utils.updateItem
import lib.atomofiron.android_window_insets_compat.applyPaddingInsets

class TextViewerFragment : Fragment(R.layout.fragment_text_viewer),
    BaseFragment<TextViewerFragment, TextViewerViewState, TextViewerPresenter> by BaseFragmentImpl()
{
    companion object {
        const val KEY_PATH = "KEY_PATH"
        const val KEY_TASK_ID = "KEY_TASK_ID"
    }

    private lateinit var binding: FragmentTextViewerBinding

    private val viewerAdapter = TextViewerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, TextViewerViewModel::class, savedInstanceState)
        viewerAdapter.textViewerListener = presenter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentTextViewerBinding.bind(view)

        binding.recyclerView.run {
            layoutManager = LinearLayoutManager(context)
            adapter = viewerAdapter
            itemAnimator = null
        }
        binding.navigationRail.menu.removeItem(R.id.stub)
        binding.navigationRail.isItemActiveIndicatorEnabled = false
        binding.navigationRail.setOnItemSelectedListener(::onBottomMenuItemClick)
        binding.bottomBar.isItemActiveIndicatorEnabled = false
        binding.bottomBar.setOnItemSelectedListener(::onBottomMenuItemClick)
        viewState.onViewCollect()
        onApplyInsets(view)
    }

    override fun TextViewerViewState.onViewCollect() {
        viewCollect(textLines, collector = viewerAdapter::setItems)
        viewCollect(currentTask, collector = ::onTaskChanged)
        viewCollect(matchesCursor, collector = ::onMatchCursorChanged)
        viewCollect(status, collector = ::onStatusChanged)
    }

    override fun onApplyInsets(root: View) {
        binding.run {
            recyclerView.applyPaddingInsets()
            OrientationLayoutDelegate(
                root as ViewGroup,
                recyclerView = recyclerView,
                bottomView = bottomBar,
                railView = navigationRail,
                systemUiView = systemUiBackground,
            ) {
                bottomBar.menu.findItem(R.id.stub).isVisible = it
            }
        }
    }

    private fun onBottomMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_search -> presenter.onSearchClick()
            R.id.menu_previous -> presenter.onPreviousClick()
            R.id.menu_next -> presenter.onNextClick()
        }
        return false
    }

    private fun onTaskChanged(task: SearchTask.Done?) {
        viewerAdapter.setMatches(task?.matchesMap)
    }

    private fun onStatusChanged(status: TextViewerViewState.Status) {
        var index: Int? = null
        var count: Int? = null
        val text = if (status.countMax == 0) null else {
            index = status.count
            count = status.countMax
            "$index / $count"
        }
        val iconId = if (status.loading) R.drawable.progress_loop else R.drawable.ic_circle_check
        binding.run {
            bottomBar.updateItem(R.id.menu_status, iconId, text)
            navigationRail.updateItem(R.id.menu_status, iconId, text)
            arrayOf(bottomBar.menu, navigationRail.menu).forEach {
                it.findItem(R.id.menu_previous).isEnabled = !status.loading && index != null && index > 1
                it.findItem(R.id.menu_next).isEnabled = !status.loading && count != null && index != count
            }
        }
    }

    private fun onMatchCursorChanged(cursor: TextViewerViewState.MatchCursor) = viewerAdapter.setCursor(cursor)
}