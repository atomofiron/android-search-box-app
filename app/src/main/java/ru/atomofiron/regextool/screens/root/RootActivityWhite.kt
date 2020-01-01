package ru.atomofiron.regextool.screens.root

import android.os.Bundle
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.base.BaseActivity
import kotlin.reflect.KClass

open class RootActivityWhite : BaseActivity<RootViewModel>() {

    override val viewModelClass: KClass<RootViewModel> = RootViewModel::class

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_root)
        super.onCreate(savedInstanceState)
    }
}