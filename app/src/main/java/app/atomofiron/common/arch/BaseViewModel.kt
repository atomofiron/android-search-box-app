package app.atomofiron.common.arch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import app.atomofiron.common.util.KObservable
import kotlinx.coroutines.CoroutineScope

abstract class BaseViewModel(app: Application) : AndroidViewModel(app) {
    lateinit var scope: CoroutineScope

    val onClearedCallback = KObservable.RemoveObserverCallback()
}
