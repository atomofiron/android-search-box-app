package app.atomofiron.searchboxapp.screens.curtain

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainPresenterParams
import javax.inject.Inject

class CurtainViewModel : BaseViewModel<CurtainComponent, CurtainFragment, CurtainViewState, CurtainPresenter>() {
    @Inject
    override lateinit var presenter: CurtainPresenter
    @Inject
    override lateinit var viewState: CurtainViewState

    override fun component(fragment: CurtainFragment): CurtainComponent {
        val params = CurtainPresenterParams.params(fragment.requireArguments())
        return DaggerCurtainComponent
            .builder()
            .bind(viewModelScope)
            .bind(fragmentProperty)
            .bind(params)
            .dependencies(DaggerInjector.appComponent)
            .build()
    }
}