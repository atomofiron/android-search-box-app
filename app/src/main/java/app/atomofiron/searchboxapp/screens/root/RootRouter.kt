package app.atomofiron.searchboxapp.screens.root

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.R

class RootRouter(property: WeakProperty<Fragment>) : BaseRouter(property) {

    override val currentDestinationId = R.id.rootFragment
}