package ru.atomofiron.regextool.screens.viewer

import app.atomofiron.common.arch.BaseViewModel
import ru.atomofiron.regextool.di.DaggerInjector

class TextViewerViewModel : BaseViewModel<TextViewerComponent, TextViewerFragment>() {
    override val component = DaggerTextViewerComponent.builder()
            .bind(this)
            .bind(viewProperty)
            .dependencies(DaggerInjector.appComponent)
            .build()

    override fun inject(view: TextViewerFragment) {
        super.inject(view)
        component.inject(this)
        component.inject(view)
    }
}