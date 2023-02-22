package app.atomofiron.searchboxapp.screens.viewer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.OrientationLayoutDelegate
import app.atomofiron.searchboxapp.databinding.FragmentTextViewerBinding
import app.atomofiron.searchboxapp.screens.viewer.recycler.TextViewerAdapter
import app.atomofiron.searchboxapp.setContentMaxWidthRes
import lib.atomofiron.android_window_insets_compat.applyPaddingInsets
import lib.atomofiron.android_window_insets_compat.insetsProxying

class TextViewerFragment : Fragment(R.layout.fragment_text_viewer),
    BaseFragment<TextViewerFragment, TextViewerViewState, TextViewerPresenter> by BaseFragmentImpl()
{
    companion object {
        const val KEY_PATH = "KEY_PATH"
        const val KEY_QUERY = "KEY_QUERY"
        const val KEY_USE_REGEX = "KEY_USE_REGEX"
        const val KEY_IGNORE_CASE = "KEY_IGNORE_CASE"
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
        binding.statusLl.setContentMaxWidthRes(R.dimen.bottom_bar_max_width)
        binding.bottomBar.setContentMaxWidthRes(R.dimen.bottom_bar_max_width)
        binding.bottomBar.isItemActiveIndicatorEnabled = false
        binding.bottomBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_search -> presenter.onSearchClick()
                R.id.menu_previous -> presenter.onPreviousClick()
                R.id.menu_next -> presenter.onNextClick()
            }
            false
        }
        viewState.onViewCollect()
        onApplyInsets(view)
    }

    override fun TextViewerViewState.onViewCollect() {
        viewCollect(loading, collector = ::setLoading)
        viewCollect(textLines, collector = viewerAdapter::setItems)
        viewCollect(matchesMap, collector = viewerAdapter::setMatches)
        viewCollect(matchesCursor, collector = ::onMatchCursorChanged)
        viewCollect(matchesCounter, collector = ::onMatchCounterChanged)
        viewCollect(insertInQuery, collector = ::insertInQuery)
    }

    override fun onApplyInsets(root: View) {
        root.insetsProxying()
        binding.recyclerView.applyPaddingInsets()
        binding.bottomAppBar.insetsProxying()
        binding.bottomBar.applyPaddingInsets(bottom = true)
        OrientationLayoutDelegate(root as ViewGroup, recyclerView = binding.recyclerView)
    }

    private fun setLoading(visible: Boolean) {
        binding.ballsView.isInvisible = !visible
        onMatchCounterChanged(viewState.matchesCounter.value)
    }

    @SuppressLint("RestrictedApi")
    private fun onMatchCounterChanged(counter: Long?) {
        var index: Int? = null
        var count: Int? = null
        binding.tvCounter.text = when (counter) {
            null -> null
            else -> {
                index = counter.shr(32).toInt()
                count = counter.toInt()
                "$index / $count"
            }
        }
        val loading = viewState.loading.value
        binding.bottomBar.menu.findItem(R.id.menu_previous).isEnabled = !loading && index != null && index!! > 1
        binding.bottomBar.menu.findItem(R.id.menu_next).isEnabled = !loading && count != null && index != count
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