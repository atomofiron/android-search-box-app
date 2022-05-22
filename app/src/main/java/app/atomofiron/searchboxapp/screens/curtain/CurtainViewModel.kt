package app.atomofiron.searchboxapp.screens.curtain

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.flow.emitNow
import app.atomofiron.common.util.flow.dataFlow
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainAction
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainPresenterParams
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import javax.inject.Inject

class CurtainViewModel : BaseViewModel<CurtainComponent, CurtainFragment, CurtainPresenter>() {
    @Inject
    override lateinit var presenter: CurtainPresenter

    var initialLayoutId = 0
        private set
    val adapter = dataFlow<CurtainApi.Adapter<*>>()
    val action = dataFlow<CurtainAction>(single = true)
    val cancelable = dataFlow(value = true)

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

    fun setCurtainAdapter(factory: CurtainApi.Adapter<*>) = this.adapter.emitNow(factory)
}