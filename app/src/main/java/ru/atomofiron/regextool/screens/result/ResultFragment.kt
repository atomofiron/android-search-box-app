package ru.atomofiron.regextool.screens.result

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
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.custom.view.BallsView
import ru.atomofiron.regextool.model.finder.FinderTask
import ru.atomofiron.regextool.model.preference.ExplorerItemComposition
import ru.atomofiron.regextool.screens.result.adapter.ResultAdapter
import ru.atomofiron.regextool.utils.setVisibility
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

    private val rvResults = Knife<RecyclerView>(this, R.id.result_rv)
    private val bView = Knife<BallsView>(this, R.id.result_bv)
    private val ivStatus = Knife<ImageView>(this, R.id.result_iv_status)
    private val tvCounter = Knife<TextView>(this, R.id.result_tv_counter)

    private val resultAdapter = ResultAdapter()

    @Inject
    override lateinit var presenter: ResultPresenter

    override fun inject() = viewModel.inject(this)

    override fun onCreate() {
        super.onCreate()

        resultAdapter.itemActionListener = presenter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvResults {
            layoutManager = LinearLayoutManager(thisContext)
            adapter = resultAdapter
        }
    }

    override fun onSubscribeData(owner: LifecycleOwner) {
        super.onSubscribeData(owner)
        viewModel.composition.observe(this, Observer(::onCompositionChange))
        viewModel.task.observe(this, Observer(::onTaskChange))
    }

    private fun onTaskChange(task: FinderTask) {
        bView {
            setVisibility(task.inProgress)
        }
        ivStatus {
            setVisibility(!task.inProgress)
            isActivated = task.isDone
        }
        tvCounter {
            text = "${task.results.size}/${task.count}"
        }
        resultAdapter.setItems(task.results)
    }

    private fun onCompositionChange(composition: ExplorerItemComposition) {
        resultAdapter.setComposition(composition)
    }
}