package app.atomofiron.searchboxapp.screens.result

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import javax.inject.Inject

class ResultViewModel : BaseViewModel<ResultComponent, ResultFragment, ResultViewState, ResultPresenter>() {

    @Inject
    override lateinit var presenter: ResultPresenter
    @Inject
    override lateinit var viewState: ResultViewState

    override fun component(fragment: ResultFragment): ResultComponent {
        val params = ResultPresenterParams.params(fragment.requireArguments())
        return DaggerResultComponent
            .builder()
            .bind(fragmentProperty)
            .bind(viewModelScope)
            .bind(params)
            .dependencies(DaggerInjector.appComponent)
            .build()
    }
}