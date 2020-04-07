package ru.atomofiron.regextool.screens.explorer

import android.content.Intent
import androidx.fragment.app.Fragment
import app.atomofiron.common.base.BaseRouter
import app.atomofiron.common.util.permission.Permissions
import ru.atomofiron.regextool.injectable.service.explorer.model.XFile
import ru.atomofiron.regextool.screens.finder.FinderFragment
import ru.atomofiron.regextool.screens.preferences.PreferenceFragment
import ru.atomofiron.regextool.utils.Util

class ExplorerRouter(fragment: Fragment) : BaseRouter() {
    private val openedFiles = ArrayList<Fragment>()

    init {
        onFragmentAttach(fragment)
    }

    fun showFinder() {
        switchScreen(addToBackStack = false) {
            it is FinderFragment
        }
    }

    fun showSettings() = startScreen(PreferenceFragment())

    fun showFile(item: XFile, extraFormats: Array<String>) {
        if (Util.isTextFile(item.completedPath, extraFormats)) {
            // todo open file
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