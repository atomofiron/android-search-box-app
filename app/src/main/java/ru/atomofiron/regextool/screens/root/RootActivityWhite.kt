package ru.atomofiron.regextool.screens.root

import android.os.Bundle
import android.view.View
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.base.BaseActivity
import ru.atomofiron.regextool.common.util.Knife
import kotlin.reflect.KClass

open class RootActivityWhite : BaseActivity<RootViewModel>() {

    override val viewModelClass: KClass<RootViewModel> = RootViewModel::class

    private val joystick = Knife<View>(this, R.id.root_iv_joystick)

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_root)
        super.onCreate(savedInstanceState)

        joystick.view.setOnClickListener {
            onBackPressed()
        }
    }

    // todo onNewIntent ACTION_SHOW_RESULT, ACTION_SHOW_RESULTS
    // todo setRequestedOrientation Util.PREF_ORIENTATION
}