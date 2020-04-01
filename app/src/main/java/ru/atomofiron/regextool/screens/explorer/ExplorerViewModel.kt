package ru.atomofiron.regextool.screens.explorer

import android.Manifest
import android.app.Application
import android.content.Context
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
import ru.atomofiron.regextool.di.DaggerInjector
import ru.atomofiron.regextool.iss.interactor.ExplorerInteractor
import ru.atomofiron.regextool.iss.service.explorer.model.Change
import ru.atomofiron.regextool.iss.service.explorer.model.XFile
import ru.atomofiron.regextool.iss.store.ExplorerStore
import ru.atomofiron.regextool.iss.store.SettingsStore
import ru.atomofiron.regextool.screens.explorer.adapter.ExplorerItemActionListener
import ru.atomofiron.regextool.screens.explorer.places.PlacesAdapter
import ru.atomofiron.regextool.screens.explorer.places.XPlace
import javax.inject.Inject

class ExplorerViewModel(app: Application) : BaseViewModel<ExplorerRouter>(app), ExplorerItemActionListener, PlacesAdapter.ItemActionListener {
    override val router = ExplorerRouter()

    @Inject
    lateinit var explorerInteractor: ExplorerInteractor

    val permissionRequiredWarning = SingleLiveEvent<Intent?>()
    val historyDrawerGravity = MutableLiveData<Int>()
    val places = LateinitLiveData<List<XPlace>>()
    val items = MutableLiveData<List<XFile>>()
    val current = MutableLiveData<XFile?>()
    val notifyUpdate = SingleLiveEvent<XFile>()
    val notifyRemove = SingleLiveEvent<XFile>()
    val notifyInsert = SingleLiveEvent<Pair<XFile, XFile>>()
    val notifyUpdateRange = SingleLiveEvent<List<XFile>>()
    val notifyRemoveRange = SingleLiveEvent<List<XFile>>()
    val notifyInsertRange = SingleLiveEvent<Pair<XFile, List<XFile>>>()

    private var readStorageGranted = false
    private lateinit var permissions: Permissions

    @Inject
    lateinit var explorerStore: ExplorerStore

    @Inject
    lateinit var settingsStore: SettingsStore

    init {
        settingsStore
                .dockGravity
                .addObserver(onClearedCallback, ::onDockGravityChanged)
        settingsStore
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

    override fun buildComponentAndInject() {
        DaggerExplorerComponent
                .builder()
                .dependencies(DaggerInjector.appComponent)
                .build()
                .inject(this)
    }

    override fun onCreate(context: Context, intent: Intent) {
        super.onCreate(context, intent)

        explorerStore.store.addObserver(onClearedCallback) {
            GlobalScope.launch(Dispatchers.Main) {
                items.value = it
            }
        }
        explorerStore.updates.addObserver(onClearedCallback) {
            GlobalScope.launch(Dispatchers.Main) {
                when (it) {
                    is Change.Update -> notifyUpdate(it.item)
                    is Change.Remove -> notifyRemove(it.item)
                    is Change.Insert -> notifyInsert(Pair(it.previous, it.item))
                    is Change.UpdateRange -> notifyUpdateRange(it.items)
                    is Change.RemoveRange -> notifyRemoveRange(it.items)
                    is Change.InsertRange -> notifyInsertRange(Pair(it.previous, it.items))
                }
            }
        }
        explorerStore.current.addObserver(onClearedCallback) {
            GlobalScope.launch(Dispatchers.Main) {
                current.value = it
            }
        }
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

    fun onItemOptionSelected(id: Int) = when (id) {
        R.id.menu_remove -> explorerInteractor.deleteItems()
        R.id.menu_rename -> Unit
        else -> Unit
    }

    fun onSettingsOptionSelected() = router.showSettings()

    fun onDockGravityChange(gravity: Int) = settingsStore.dockGravity.push(gravity)

    override fun onItemClick(item: XFile) {
        val useSu = settingsStore.useSu.value
        when {
            !useSu && !readStorageGranted -> permissions
                    .check(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .granted {
                        readStorageGranted = true
                        onItemClick(item)
                    }
                    .forbidden { permissionRequiredWarning.invoke() }
            item.isDirectory -> explorerInteractor.openDir(item)
            else -> {
                val extraFormats = settingsStore.extraFormats.entity
                router.showFile(item, extraFormats)
            }
        }
    }

    override fun onItemCheck(item: XFile, isChecked: Boolean) = explorerInteractor.checkItem(item, isChecked)

    override fun onItemVisible(item: XFile) = explorerInteractor.updateItem(item)

    override fun onItemInvalidate(item: XFile) = explorerInteractor.invalidateItem(item)

    fun onAllowStorageClick() = router.showSystemPermissionsAppSettings()
}