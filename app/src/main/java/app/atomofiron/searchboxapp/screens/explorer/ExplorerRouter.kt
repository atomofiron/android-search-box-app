package app.atomofiron.searchboxapp.screens.explorer

import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.permission.PermissionDelegate
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.android.Intents
import app.atomofiron.searchboxapp.injectable.router.FileSharingDelegate
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.finder.FinderFragment
import app.atomofiron.searchboxapp.screens.viewer.presenter.TextViewerParams
import app.atomofiron.searchboxapp.utils.Util

class ExplorerRouter(
    property: WeakProperty<Fragment>,
    fileSharingDelegate: FileSharingDelegate,
) : BaseRouter(property), FileSharingDelegate by fileSharingDelegate {

    override val currentDestinationId = R.id.rootFragment

    val permissions = PermissionDelegate.create(activityProperty, property)

    fun showFinder() {
        fragment {
            parentFragmentManager.switchScreen { it is FinderFragment }
        }
    }

    fun showSettings() = navigate(R.id.preferenceFragment)

    fun showFile(item: Node, textFormats: Array<String>) {
        if (Util.isTextFile(item.path, textFormats)) {
            val arguments = TextViewerParams.arguments(item.path)
            navigate(R.id.textViewerFragment, arguments)
        } else {
            activity {
                val intent = Intent(Intent.ACTION_VIEW)

            }
        }
    }

    fun showSystemPermissionsAppSettings() {
        activity {
            when {
                SDK_INT >= Android.R -> startActivity(Intents.storagePermissionIntent)
                else -> startActivity(Intents.settingsIntent)
            }
        }
    }
}