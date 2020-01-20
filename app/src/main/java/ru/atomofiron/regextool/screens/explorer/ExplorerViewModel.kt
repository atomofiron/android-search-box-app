package ru.atomofiron.regextool.screens.explorer

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.common.base.BaseViewModel
import ru.atomofiron.regextool.common.util.LiveEvent
import ru.atomofiron.regextool.iss.interactor.ExplorerInteractor
import ru.atomofiron.regextool.iss.service.model.XFile
import ru.atomofiron.regextool.screens.explorer.adapter.ItemActionListener

class ExplorerViewModel(app: Application) : BaseViewModel<ExplorerRouter>(app), ItemActionListener {
    override val router = ExplorerRouter()

    private val explorerInteractor = ExplorerInteractor()

    val files = MutableLiveData<List<XFile>>()
    val notifyUpdated = LiveEvent<XFile?>()

    override fun onCreate(context: Context, intent: Intent) {
        super.onCreate(context, intent)

        explorerInteractor.observeFiles {
            GlobalScope.launch(Dispatchers.Main) {
                files.value = it
            }
        }
        explorerInteractor.observeUpdates {
            GlobalScope.launch(Dispatchers.Main) {
                notifyUpdated(it)
            }
        }
    }

    fun onSearchOptionSelected() = router.showFinder()

    fun onOptionsOptionSelected() {
    }

    fun onSettingsOptionSelected() = router.showSettings()

    override fun onItemClick(item: XFile) {
        when {
            !item.isDirectory -> Unit
            item.isOpened -> explorerInteractor.closeDir(item)
            else -> explorerInteractor.openDir(item)
        }
    }

    override fun onItemVisible(item: XFile) = explorerInteractor.updateFile(item)

    override fun onItemInvalidate(item: XFile) = explorerInteractor.invalidateFile(item)
}