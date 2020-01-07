package ru.atomofiron.regextool.screens.explorer

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import ru.atomofiron.regextool.common.base.BaseViewModel
import ru.atomofiron.regextool.iss.interactor.ExplorerInteractor
import ru.atomofiron.regextool.iss.service.model.XFile

class ExplorerViewModel(app: Application) : BaseViewModel<ExplorerRouter>(app) {
    override val router = ExplorerRouter()

    private val explorerInteractor = ExplorerInteractor()

    val files = MutableLiveData<List<XFile>>()

    override fun onCreate(context: Context, intent: Intent) {
        super.onCreate(context, intent)

        files.value = explorerInteractor.getFiles()
    }

    fun onSearchOptionSelected() = router.showFinder()

    fun onOptionsOptionSelected() {
    }

    fun onSettingsOptionSelected() = router.showSettings()

    fun onItemClicked(position: Int) {
        val item = files.value!![position]
        when {
            !item.file.isDirectory -> Unit
            item.opened -> explorerInteractor.closeDir(item) {
                files.value = it
            }
            else -> explorerInteractor.openDir(item) {
                files.value = it
            }
        }
    }
}