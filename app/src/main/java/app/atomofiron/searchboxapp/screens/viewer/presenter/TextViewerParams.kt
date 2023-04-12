package app.atomofiron.searchboxapp.screens.viewer.presenter

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.os.Parcelable
import androidx.core.app.BundleCompat
import app.atomofiron.searchboxapp.model.finder.SearchParams
import app.atomofiron.searchboxapp.screens.viewer.TextViewerFragment

class TextViewerParams(
    val path: String,
    val initialParams: SearchParams?,
) {
    companion object {
        private const val KEY_PARAMS = "KEY_PARAMS"

        fun arguments(path: String, taskId: Int? = null) = Bundle().apply {
            putString(TextViewerFragment.KEY_PATH, path)
            if (taskId != null) putInt(TextViewerFragment.KEY_TASK_ID, taskId)
        }

        fun params(arguments: Bundle): TextViewerParams {
            return TextViewerParams(
                arguments.getString(TextViewerFragment.KEY_PATH)!!,
                arguments.getParcelableCompat(KEY_PARAMS, SearchParams::class.java),
            )
        }
    }
}

inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String, clazz: Class<T>): T? = when {
    SDK_INT >= TIRAMISU -> getParcelable(key, clazz)
    else -> getParcelable(key)
}
