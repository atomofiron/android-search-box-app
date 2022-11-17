package app.atomofiron.searchboxapp.screens.explorer.presenter

import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewState
import app.atomofiron.searchboxapp.screens.explorer.places.PlacesAdapter
import app.atomofiron.searchboxapp.screens.explorer.places.XPlace

class PlacesActionListenerDelegate(
    private val viewState: ExplorerViewState,
) : PlacesAdapter.ItemActionListener {

    override fun onItemClick(item: XPlace) {
        viewState.places.value = viewState.places.value.plusElement(XPlace.AnotherPlace("${Math.random()}"))
    }

    override fun onItemActionClick(item: XPlace) {
        when (item) {
            is XPlace.InternalStorage -> viewState.places.value = viewState.places.value.map {
                if (it == item) XPlace.InternalStorage(item.title, !item.visible) else it
            }
            is XPlace.ExternalStorage -> viewState.places.value = viewState.places.value.map {
                if (it == item) XPlace.ExternalStorage(item.title, !item.visible) else it
            }
            is XPlace.AnotherPlace -> viewState.places.value = viewState.places.value.filter { it != item }
            is XPlace.StoragePlace -> Unit
        }
    }
}