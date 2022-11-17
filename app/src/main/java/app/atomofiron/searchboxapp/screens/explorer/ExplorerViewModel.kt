package app.atomofiron.searchboxapp.screens.explorer

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel2
import app.atomofiron.searchboxapp.di.DaggerInjector
import javax.inject.Inject

class ExplorerViewModel : BaseViewModel2<ExplorerComponent, ExplorerFragment, ExplorerViewState, ExplorerPresenter>() {

    @Inject
    override lateinit var viewState: ExplorerViewState
    @Inject
    override lateinit var presenter: ExplorerPresenter
    @Inject
    lateinit var router: ExplorerRouter

    override val component = DaggerExplorerComponent
        .builder()
        .bind(fragmentProperty)
        .bind(viewModelScope)
        .dependencies(DaggerInjector.appComponent)
        .build().apply {
            inject(this@ExplorerViewModel)
        }

    override fun setFragment(view: ExplorerFragment) {
        super.setFragment(view)
        router.permissions.registerForActivityResult(view)
    }
}