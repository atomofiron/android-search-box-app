package app.atomofiron.searchboxapp.screens.root

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import kotlinx.coroutines.CoroutineScope

class RootPresenter(
    scope: CoroutineScope,
    router: RootRouter,
    preferenceStore: PreferenceStore,
) : BasePresenter<RootViewModel, RootRouter>(scope, router) {

    override fun onSubscribeData() = Unit

    fun onBack() = router.onBack()
}
