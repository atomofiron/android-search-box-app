package app.atomofiron.searchboxapp.screens.explorer

import android.content.Intent
import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.permission.Permissions
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.screens.finder.FinderFragment
import app.atomofiron.searchboxapp.screens.viewer.presenter.TextViewerParams
import app.atomofiron.searchboxapp.utils.Util

class ExplorerRouter(property: WeakProperty<Fragment>) : BaseRouter(property) {

    override val currentDestinationId = R.id.rootFragment

    fun showFinder() {
        switchScreen {
            it is FinderFragment
        }
    }

    fun showSettings() = navigate(R.id.preferenceFragment)

    fun showFile(item: XFile, textFormats: Array<String>) {
        if (Util.isTextFile(item.completedPath, textFormats)) {
            val arguments = TextViewerParams.arguments(item.completedPath)
            navigate(R.id.textViewerFragment, arguments)
        } else {
            activity {
                val intent = Intent(Intent.ACTION_VIEW)

            }
        }
    }

    fun showSystemPermissionsAppSettings() {
        activity {
            startActivity(Permissions.settingsIntent)
        }
    }
}