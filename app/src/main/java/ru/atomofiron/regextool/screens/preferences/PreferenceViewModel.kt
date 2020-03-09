package ru.atomofiron.regextool.screens.preferences

import android.app.Application
import app.atomofiron.common.base.BaseViewModel

class PreferenceViewModel(app: Application) : BaseViewModel<PreferenceRouter>(app) {
    override val router = PreferenceRouter()
}