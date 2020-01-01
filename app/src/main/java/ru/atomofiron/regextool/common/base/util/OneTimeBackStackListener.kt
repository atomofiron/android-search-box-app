package ru.atomofiron.regextool.common.base.util

import androidx.fragment.app.FragmentManager
import ru.atomofiron.regextool.log

class OneTimeBackStackListener(
    private val manager: FragmentManager,
    private var callback: (() -> Unit)?
) : FragmentManager.OnBackStackChangedListener {
    init {
        manager.addOnBackStackChangedListener(this)
    }
    override fun onBackStackChanged() {
        log("onBackStackChanged")
        callback?.invoke()
        callback = null
        manager.removeOnBackStackChangedListener(this)
    }
}