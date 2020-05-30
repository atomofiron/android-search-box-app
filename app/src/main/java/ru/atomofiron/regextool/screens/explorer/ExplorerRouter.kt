package ru.atomofiron.regextool.screens.explorer

import android.content.Intent
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.permission.Permissions
import app.atomofiron.common.util.property.WeakProperty
import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.screens.finder.FinderFragment
import ru.atomofiron.regextool.screens.preferences.PreferenceFragment
import ru.atomofiron.regextool.screens.viewer.TextViewerFragment
import ru.atomofiron.regextool.utils.Util

class ExplorerRouter(property: WeakProperty<ExplorerFragment>) : BaseRouter(property) {

    fun showFinder() {
        switchScreen(addToBackStack = false) {
            it is FinderFragment
        }
    }

    fun showSettings() = startScreen(PreferenceFragment())

    fun showFile(item: XFile, textFormats: Array<String>) {
        if (Util.isTextFile(item.completedPath, textFormats)) {
            val fragment = TextViewerFragment.openTextFile(item.completedPath)
            startScreen(fragment)
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