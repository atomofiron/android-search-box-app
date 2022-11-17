package app.atomofiron.searchboxapp.screens.curtain

import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.DataFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainAction
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainPresenterParams
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class CurtainViewModel : BaseViewModel<CurtainComponent, CurtainFragment, CurtainPresenter>() {
    @Inject
    override lateinit var presenter: CurtainPresenter

    var initialLayoutId = 0
        private set
    val adapter = DataFlow<CurtainApi.Adapter<*>>()
    val action = ChannelFlow<CurtainAction>()
    val cancelable = MutableStateFlow(true)

    private lateinit var params: CurtainPresenterParams

    override fun inject(view: CurtainFragment) {
        params = CurtainPresenterParams.params(view.requireArguments())
        initialLayoutId = params.layoutId

        super.inject(view)

        component.inject(this)
    }

    override fun createComponent(fragmentProperty: WeakProperty<Fragment>) = DaggerCurtainComponent
        .builder()
        .bind(this)
        .bind(fragmentProperty)
        .bind(params)
        .dependencies(DaggerInjector.appComponent)
        .build()

    fun setCurtainAdapter(factory: CurtainApi.Adapter<*>) {
        adapter[viewModelScope] = factory
    }
}