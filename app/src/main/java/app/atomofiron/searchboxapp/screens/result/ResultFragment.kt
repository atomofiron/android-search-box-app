package app.atomofiron.searchboxapp.screens.result

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.arch.fragment.BaseFragment
import app.atomofiron.common.util.Knife
import app.atomofiron.common.util.flow.viewCollect
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.BallsView
import app.atomofiron.searchboxapp.custom.view.BottomMenuBar
import app.atomofiron.searchboxapp.custom.view.bottom_sheet.BottomSheetView
import app.atomofiron.searchboxapp.model.finder.FinderTask
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.sheet.BottomSheetMenuWithTitle
import app.atomofiron.searchboxapp.screens.result.adapter.ResultAdapter
import app.atomofiron.searchboxapp.utils.setVisibility
import javax.inject.Inject
import kotlin.reflect.KClass

class ResultFragment : BaseFragment<ResultViewModel, ResultPresenter>() {
    companion object {
        const val KEY_TASK_ID = "KEY_TASK_ID"

        fun create(taskId: Long): ResultFragment {
            val fragment = ResultFragment()
            val arguments = Bundle()
            arguments.putLong(KEY_TASK_ID, taskId)
            fragment.arguments = arguments
            return fragment
        }
    }
    override val viewModelClass: KClass<ResultViewModel> = ResultViewModel::class
    override val layoutId: Int = R.layout.fragment_result

    private val mbmBar = Knife<BottomMenuBar>(this, R.id.result_bmb)
    private val bottomSheetView = Knife<BottomSheetView>(this, R.id.result_bsv)
    private val rvResults = Knife<RecyclerView>(this, R.id.result_rv)
    private val bView = Knife<BallsView>(this, R.id.result_bv)
    private val ivStatus = Knife<ImageView>(this, R.id.result_iv_status)
    private val tvCounter = Knife<TextView>(this, R.id.result_tv_counter)

    private val resultAdapter = ResultAdapter()
    private lateinit var bottomItemMenu: BottomSheetMenuWithTitle
    private val errorSnackbar: Snackbar by lazy(LazyThreadSafetyMode.NONE) {
        Snackbar.make(thisView, "", Snackbar.LENGTH_INDEFINITE)
                .setAnchorView(anchorView)
                .setAction(R.string.dismiss) {
                    errorSnackbar.dismiss()
                    presenter.onDropTaskErrorClick()
                }
    }
    private var snackbarError: String? = null

    @Inject
    override lateinit var presenter: ResultPresenter

    override fun inject() = viewModel.inject(this)

    override fun onCreate() {
        super.onCreate()

        resultAdapter.itemActionListener = presenter
        bottomItemMenu = BottomSheetMenuWithTitle(R.menu.item_options_result, thisContext, presenter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvResults {
            itemAnimator = null
            layoutManager = LinearLayoutManager(thisContext)
            adapter = resultAdapter
        }
        mbmBar {
            setOnMenuItemClickListener(::onBottomMenuItemClick)
        }
        bottomItemMenu.bottomSheetView = bottomSheetView.view
        onViewCollect()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        rvResults {
            adapter = null
        }
    }

    private fun onBottomMenuItemClick(id: Int) {
        when (id) {
            R.id.menu_stop -> presenter.onStopClick()
            R.id.menu_options -> presenter.onOptionsClick()
            R.id.menu_export -> presenter.onExportClick()
        }
    }

    private fun onViewCollect() = viewModel.apply {
        viewCollect(composition, ::onCompositionChange)
        viewCollect(task, ::onTaskChange)
        viewCollect(enableOptions, ::enableOptions)
        viewCollect(showOptions, ::showOptions)
        viewCollect(notifyTaskHasChanged) { resultAdapter.notifyDataSetChanged() }
        viewCollect(notifyItemChanged, resultAdapter::setItem) // todo works poor
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        resultAdapter.notifyItemChanged(0)
    }

    @SuppressLint("RestrictedApi")
    private fun onTaskChange(task: FinderTask) {
        bView {
            setVisibility(task.inProgress)
        }
        ivStatus {
            setVisibility(!task.inProgress)
            isActivated = task.isDone
            isEnabled = task.error == null
        }
        tvCounter {
            text = "${task.results.size}/${task.count}"
        }
        mbmBar {
            var item = menu.findItem(R.id.menu_stop)
            if (item.isEnabled != task.inProgress) {
                item.isEnabled = task.inProgress
            }
            item = menu.findItem(R.id.menu_export)
            if (item.isEnabled != task.results.isNotEmpty()) {
                item.isEnabled = task.results.isNotEmpty()
            }
        }
        resultAdapter.setResults(task.results)

        if (task.results.isNotEmpty()) {
            // fix first item offset
            resultAdapter.notifyItemChanged(0)
        }

        if (task.error != snackbarError) {
            errorSnackbar.setText(task.error!!).show()
        }
    }

    private fun onCompositionChange(composition: ExplorerItemComposition) {
        resultAdapter.setComposition(composition)
    }

    @SuppressLint("RestrictedApi")
    private fun enableOptions(enable: Boolean) {
        mbmBar {
            val item = menu.findItem(R.id.menu_options)
            if (item.isEnabled != enable) {
                item.isEnabled = enable
            }
        }
    }

    private fun showOptions(options: ExplorerItemOptions) {
        bottomItemMenu.show(options)
        if (options.items.size == 1) {
            bottomItemMenu.tvDescription.visibility = View.VISIBLE
            bottomItemMenu.tvDescription.text = options.items[0].completedPath
        } else {
            bottomItemMenu.tvDescription.visibility = View.GONE
        }
    }
}