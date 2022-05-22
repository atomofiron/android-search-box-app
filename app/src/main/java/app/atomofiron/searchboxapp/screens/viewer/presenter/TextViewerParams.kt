package app.atomofiron.searchboxapp.screens.viewer.presenter

import android.os.Bundle
import app.atomofiron.searchboxapp.model.finder.FinderQueryParams
import app.atomofiron.searchboxapp.screens.viewer.TextViewerFragment

class TextViewerParams(
    val path: String,
    val query: String,
    val useRegex: Boolean,
    val ignoreCase: Boolean,
) {
    companion object {

        fun arguments(path: String, params: FinderQueryParams? = null) = Bundle().apply {
            putString(TextViewerFragment.KEY_PATH, path)
            putString(TextViewerFragment.KEY_QUERY, params?.query)
            putBoolean(TextViewerFragment.KEY_USE_REGEX, params?.useRegex ?: false)
            putBoolean(TextViewerFragment.KEY_IGNORE_CASE, params?.ignoreCase ?: false)
        }

        fun params(arguments: Bundle) = TextViewerParams(
            arguments.getString(TextViewerFragment.KEY_PATH)!!,
            arguments.getString(TextViewerFragment.KEY_QUERY)!!,
            arguments.getBoolean(TextViewerFragment.KEY_USE_REGEX),
            arguments.getBoolean(TextViewerFragment.KEY_IGNORE_CASE),
        )
    }
}