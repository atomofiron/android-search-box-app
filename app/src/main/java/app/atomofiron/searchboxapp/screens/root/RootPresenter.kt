package app.atomofiron.searchboxapp.screens.root

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore

class RootPresenter(
    viewModel: RootViewModel,
    router: RootRouter,
    preferenceStore: PreferenceStore,
) : BasePresenter<RootViewModel, RootRouter>(viewModel, router) {

    override fun onSubscribeData() = Unit

    fun onBack() = router.onBack()
}
