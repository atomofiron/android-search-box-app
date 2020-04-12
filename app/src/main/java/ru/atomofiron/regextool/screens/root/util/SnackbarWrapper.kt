package ru.atomofiron.regextool.screens.root.util

import android.content.Context
import app.atomofiron.common.util.findBooleanByAttr
import com.google.android.material.snackbar.Snackbar
import ru.atomofiron.regextool.R

class SnackbarWrapper(
        private val context: Context,
        private val factory: () -> Snackbar
) {
    private var snackbar: Snackbar? = null

    fun show() {
        if (snackbar == null) {
            snackbar = factory()
        } else {
            val isDark = snackbar!!.context.findBooleanByAttr(R.attr.isDarkTheme)
            val isDarkNow = context.findBooleanByAttr(R.attr.isDarkTheme)
            if (isDark xor isDarkNow) {
                snackbar = factory()
            }
        }
        snackbar!!.show()
    }
}