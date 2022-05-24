package app.atomofiron.searchboxapp.screens.viewer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.flow.value
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.common.util.insets.ViewGroupInsetsProxy
import app.atomofiron.common.util.insets.ViewInsetsController
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.FragmentTextViewerBinding
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

    private lateinit var binding: FragmentTextViewerBinding

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

        binding = FragmentTextViewerBinding.bind(view)

        binding.recyclerView.run {
            layoutManager = LinearLayoutManager(context)
            adapter = viewerAdapter
            itemAnimator = null
        }
        binding.bottomBar.setOnMenuItemClickListener { id ->
            when (id) {
                R.id.menu_search -> searchDelegate.show(viewModel.xFile, viewModel.composition)
                R.id.menu_previous -> presenter.onPreviousClick()
                R.id.menu_next -> presenter.onNextClick()
            }
        }
        searchDelegate.bottomSheetView = binding.bottomSheet
        viewModel.onViewCollect()
        onApplyInsets(view)
    }

    override fun onStart() {
        super.onStart()
        binding.bottomAppBar.updateElevation()
    }

    override fun TextViewerViewModel.onViewCollect() {
        viewCollect(loading, collector = ::setLoading)
        viewCollect(textLines, collector = viewerAdapter::setItems)
        viewCollect(matchesMap, collector = viewerAdapter::setMatches)
        viewCollect(matchesCursor, collector = ::onMatchCursorChanged)
        viewCollect(matchesCounter, collector = ::onMatchCounterChanged)
        viewCollect(searchItems, collector = searchDelegate::setItems)
        viewCollect(insertInQuery, collector = ::insertInQuery)
        viewCollect(closeBottomSheet, collector = this@TextViewerFragment::closeBottomSheet)
    }

    override fun onApplyInsets(root: View) {
        ViewGroupInsetsProxy.set(root)
        ViewGroupInsetsProxy.set(binding.bottomSheet)
        ViewInsetsController.bindPadding(binding.recyclerView, start = true, top = true, end = true, bottom = true)
        ViewInsetsController.bindPadding(binding.bottomAppBar, bottom = true)
    }

    override fun onBack(): Boolean = binding.bottomSheet.hide() || super.onBack()

    private fun setLoading(visible: Boolean) {
        binding.ballsView.isInvisible = !visible
        onMatchCounterChanged(viewModel.matchesCounter.value)
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
        val loading = viewModel.loading.value
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

    private fun closeBottomSheet(unit: Unit = Unit) {
        binding.bottomSheet.hide()
    }
}