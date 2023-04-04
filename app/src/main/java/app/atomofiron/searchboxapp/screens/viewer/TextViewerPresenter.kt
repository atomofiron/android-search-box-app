package app.atomofiron.searchboxapp.screens.viewer

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.injectable.interactor.TextViewerInteractor
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.viewer.presenter.SearchAdapterPresenterDelegate
import app.atomofiron.searchboxapp.screens.viewer.presenter.TextViewerParams
import app.atomofiron.searchboxapp.screens.viewer.recycler.TextViewerAdapter
import kotlinx.coroutines.CoroutineScope

class TextViewerPresenter(
    params: TextViewerParams,
    scope: CoroutineScope,
    private val viewState: TextViewerViewState,
    router: TextViewerRouter,
    private val searchDelegate: SearchAdapterPresenterDelegate,
    private val interactor: TextViewerInteractor,
    session: TextViewerSession,
) : BasePresenter<TextViewerViewModel, TextViewerRouter>(scope, router),
    TextViewerAdapter.TextViewerListener,
    FinderAdapterOutput by searchDelegate
{

    private val item: Node get() = viewState.item.value

    init {
        session.tasks.collect(scope) {
            viewState.setTasks(it)
        }
        session.textLoading.collect(scope, viewState::setLoading)
        params.taskId?.let { taskId ->
            interactor.fetchTask(item, taskId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        interactor.closeSession(item)
    }

    override fun onSubscribeData() = Unit

    override fun onLineVisible(index: Int) = interactor.readFileToLine(item, index)

    fun onSearchClick() = searchDelegate.show()

    fun onPreviousClick() {
        viewState.changeCursor(increment = false)
    }

    fun onNextClick() {
        val requiredLineIndex = viewState.changeCursor(increment = true)
        if (requiredLineIndex >= 0) {
            interactor.readFileToLine(item, requiredLineIndex) {
                viewState.changeCursor(increment = true)
            }
        }
    }
}