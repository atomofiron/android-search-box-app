package app.atomofiron.searchboxapp.injectable.interactor

import android.content.Context
import androidx.core.content.FileProvider
import app.atomofiron.searchboxapp.BuildConfig
import app.atomofiron.searchboxapp.injectable.service.ApkService
import app.atomofiron.searchboxapp.model.explorer.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ApkInteractor(
    private val context: Context,
    private val scope: CoroutineScope,
    private val service: ApkService,
) {
    fun installApk(item: Node) {
        scope.launch(Dispatchers.IO) {
            val file = File(item.path)
            val uri = FileProvider.getUriForFile(context, BuildConfig.AUTHORITY, file)
            service.installApk2(uri)
        }
    }
}