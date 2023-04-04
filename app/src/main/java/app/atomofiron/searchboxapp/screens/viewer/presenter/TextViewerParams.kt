package app.atomofiron.searchboxapp.screens.viewer.presenter

import android.os.Bundle
import app.atomofiron.searchboxapp.screens.viewer.TextViewerFragment

class TextViewerParams(
    val path: String,
    val taskId: Long?,
) {
    companion object {

        fun arguments(path: String, taskId: Long? = null) = Bundle().apply {
            putString(TextViewerFragment.KEY_PATH, path)
            if (taskId != null) putLong(TextViewerFragment.KEY_TASK_ID, taskId)
        }

        fun params(arguments: Bundle) = TextViewerParams(
            arguments.getString(TextViewerFragment.KEY_PATH)!!,
            arguments.getLong(TextViewerFragment.KEY_TASK_ID, 0).takeIf { it > 0 },
        )
    }
}