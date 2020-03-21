package ru.atomofiron.regextool.screens.explorer

import android.Manifest
import android.app.Application
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import app.atomofiron.common.base.BaseViewModel
import app.atomofiron.common.util.LateinitLiveData
import app.atomofiron.common.util.SingleLiveEvent
import app.atomofiron.common.util.permission.Permissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.iss.interactor.ExplorerInteractor
import ru.atomofiron.regextool.iss.service.explorer.model.Change
import ru.atomofiron.regextool.iss.service.explorer.model.XFile
import ru.atomofiron.regextool.iss.store.SettingsStore
import ru.atomofiron.regextool.screens.explorer.adapter.ExplorerItemActionListener
import ru.atomofiron.regextool.screens.explorer.places.PlacesAdapter
import ru.atomofiron.regextool.screens.explorer.places.XPlace

class ExplorerViewModel(app: Application) : BaseViewModel<ExplorerRouter>(app), ExplorerItemActionListener, PlacesAdapter.ItemActionListener {
    override val router = ExplorerRouter()

    private val explorerInteractor = ExplorerInteractor()

    val permissionRequiredWarning = SingleLiveEvent<Intent?>()
    val historyDrawerGravity = MutableLiveData<Int>()
    val places = LateinitLiveData<List<XPlace>>()
    val items = MutableLiveData<List<XFile>>()
    val notifyCurrent = SingleLiveEvent<XFile?>()
    val notifyUpdate = SingleLiveEvent<XFile>()
    val notifyRemove = SingleLiveEvent<XFile>()
    val notifyInsert = SingleLiveEvent<Pair<XFile, XFile>>()
    val notifyRemoveRange = SingleLiveEvent<List<XFile>>()
    val notifyInsertRange = SingleLiveEvent<Pair<XFile, List<XFile>>>()

    private var readStorageGranted = false
    private lateinit var permissions: Permissions

    init {
        explorerInteractor.observeItems {
            GlobalScope.launch(Dispatchers.Main) {
                items.value = it
            }
        }
        explorerInteractor.observeUpdates {
            GlobalScope.launch(Dispatchers.Main) {
                when (it) {
                    is Change.Current -> notifyCurrent(it.file)
                    is Change.Update -> notifyUpdate(it.file)
                    is Change.Remove -> notifyRemove(it.file)
                    is Change.Insert -> notifyInsert(Pair(it.previous, it.file))
                    is Change.RemoveRange -> notifyRemoveRange(it.files)
                    is Change.InsertRange -> notifyInsertRange(Pair(it.previous, it.files))
                }
            }
        }
        SettingsStore
                .dockGravity
                .addObserver(onClearedCallback, ::onDockGravityChanged)
        SettingsStore
                .storagePath
                .addObserver(onClearedCallback, ::onStoragePathChanged)

        val items = ArrayList<XPlace>()
        items.add(XPlace.InternalStorage(app.getString(R.string.internal_storage), visible = true))
        items.add(XPlace.ExternalStorage(app.getString(R.string.external_storage), visible = true))
        items.add(XPlace.AnotherPlace("Another Place 0"))
        items.add(XPlace.AnotherPlace("Another Place 1"))
        items.add(XPlace.AnotherPlace("Another Place 2"))
        places.value = items
    }

    override fun onFragmentAttach(fragment: Fragment) {
        super.onFragmentAttach(fragment)
        permissions = Permissions(fragment)
    }

    override fun onItemClick(item: XPlace) {
        places.value = places.value.plusElement(XPlace.AnotherPlace("${Math.random()}"))
    }

    override fun onItemActionClick(item: XPlace) {
        when (item) {
            is XPlace.InternalStorage -> places.value = places.value.map {
                if (it == item) XPlace.InternalStorage(item.title, !item.visible) else it
            }
            is XPlace.ExternalStorage -> places.value = places.value.map {
                if (it == item) XPlace.ExternalStorage(item.title, !item.visible) else it
            }
            is XPlace.AnotherPlace -> places.value = places.value.filter { it != item }
        }
    }

    override fun onCleared() {
        super.onCleared()

        explorerInteractor.scope.cancel("${this.javaClass.simpleName}.onCleared()")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        this.permissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun onDockGravityChanged(gravity: Int) {
        historyDrawerGravity.value = gravity
    }

    private fun onStoragePathChanged(path: String) = explorerInteractor.setRoot(path)

    fun onSearchOptionSelected() = router.showFinder()

    fun onOptionsOptionSelected() {
    }

    fun onSettingsOptionSelected() = router.showSettings()

    fun onDockGravityChange(gravity: Int) = SettingsStore.dockGravity.push(gravity)

    override fun onItemClick(item: XFile) {
        val useSu = SettingsStore.useSu.value
        when {
            !useSu && !readStorageGranted -> permissions
                    .check(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .granted {
                        readStorageGranted = true
                        onItemClick(item)
                    }
                    .forbidden { permissionRequiredWarning.invoke() }
            item.isDirectory -> explorerInteractor.openDir(item)
            else -> router.showFile(item)
        }
    }

    override fun onItemCheck(item: XFile, isChecked: Boolean) = explorerInteractor.checkItem(item, isChecked)

    override fun onItemVisible(item: XFile) = explorerInteractor.updateItem(item)

    override fun onItemInvalidate(item: XFile) = explorerInteractor.invalidateItem(item)

    fun onAllowStorageClick() = router.showSystemPermissionsAppSettings()
}