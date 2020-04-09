package app.atomofiron.common.arch

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

abstract class BaseRouter private constructor() : app.atomofiron.common.base.BaseRouter() {
    constructor(fragment: Fragment) : this() {
        // legacy
        onFragmentAttach(fragment)
    }

    constructor(activity: AppCompatActivity) : this() {
        // legacy
        onActivityAttach(activity)
    }
}