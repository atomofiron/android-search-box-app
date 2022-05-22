package app.atomofiron.searchboxapp.screens.explorer.presenter

import app.atomofiron.common.util.flow.value
import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewModel
import app.atomofiron.searchboxapp.screens.explorer.places.PlacesAdapter
import app.atomofiron.searchboxapp.screens.explorer.places.XPlace

class PlacesActionListenerDelegate(
    private val viewModel: ExplorerViewModel
) : PlacesAdapter.ItemActionListener {

    override fun onItemClick(item: XPlace) {
        viewModel.places.value = viewModel.places.value.plusElement(XPlace.AnotherPlace("${Math.random()}"))
    }

    override fun onItemActionClick(item: XPlace) {
        when (item) {
            is XPlace.InternalStorage -> viewModel.places.value = viewModel.places.value.map {
                if (it == item) XPlace.InternalStorage(item.title, !item.visible) else it
            }
            is XPlace.ExternalStorage -> viewModel.places.value = viewModel.places.value.map {
                if (it == item) XPlace.ExternalStorage(item.title, !item.visible) else it
            }
            is XPlace.AnotherPlace -> viewModel.places.value = viewModel.places.value.filter { it != item }
        }
    }
}