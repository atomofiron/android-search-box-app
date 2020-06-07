package ru.atomofiron.regextool.screens.viewer

import android.os.Bundle
import android.view.View
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
import ru.atomofiron.regextool.model.finder.FinderQueryParams
import ru.atomofiron.regextool.screens.viewer.recycler.TextViewerAdapter
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

    private val rvTextViewer = Knife<RecyclerView>(this, R.id.text_viewer_rv)
    private val bvLoading = Knife<BallsView>(this, R.id.text_viewer_bv)
    private val bottomMenuBar = Knife<BottomMenuBar>(this, R.id.text_viewer_bmb)

    @Inject
    override lateinit var presenter: TextViewerPresenter

    private val viewerAdapter = TextViewerAdapter()

    override fun inject() = viewModel.inject(this)

    override fun onCreate() {
        super.onCreate()

        viewerAdapter.textViewerListener = presenter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvTextViewer {
            layoutManager = LinearLayoutManager(context)
            adapter = viewerAdapter
        }
        bottomMenuBar {
            setOnMenuItemClickListener { id ->
                when (id) {
                    R.id.menu_search -> presenter.onSearchClick()
                    R.id.menu_previous -> presenter.onPreviousClick()
                    R.id.menu_next -> presenter.onNextClick()
                }
            }
        }
    }

    override fun onSubscribeData(owner: LifecycleOwner) {
        super.onSubscribeData(owner)

        viewModel.loading.observe(owner, Observer(::setLoading))
        viewModel.textLines.observe(owner, Observer(viewerAdapter::setItems))
        viewModel.matches.observe(owner, Observer(viewerAdapter::setMatches))
        viewModel.matchesCursor.observe(owner, Observer(::onMatchCursorChanged))
    }

    private fun setLoading(visible: Boolean) {
        bvLoading {
            setVisible(visible)
        }
    }

    private fun onMatchCursorChanged(cursor: Long?) {
        viewerAdapter.setCursor(cursor)
        bottomMenuBar {
            menu.findItem(R.id.menu_previous).isEnabled = cursor != null
            menu.findItem(R.id.menu_next).isEnabled = cursor != null
        }
    }
}