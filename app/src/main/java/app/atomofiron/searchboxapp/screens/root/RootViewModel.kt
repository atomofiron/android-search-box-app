package app.atomofiron.searchboxapp.screens.root

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.di.DaggerInjector
import javax.inject.Inject

class RootViewModel : BaseViewModel<RootComponent, RootFragment, RootPresenter>() {

    @Inject
    override lateinit var presenter: RootPresenter

    override fun inject(view: RootFragment) {
        super.inject(view)
        component.inject(this)
    }

    override fun createComponent(fragmentProperty: WeakProperty<Fragment>) = DaggerRootComponent
        .builder()
        .bind(this)
        .bind(fragmentProperty)
        .dependencies(DaggerInjector.appComponent)
        .build()
}