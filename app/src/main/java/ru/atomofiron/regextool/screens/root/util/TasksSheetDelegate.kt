package ru.atomofiron.regextool.screens.root.util

import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.log
import ru.atomofiron.regextool.screens.root.RootActivity
import ru.atomofiron.regextool.view.custom.Joystick
import ru.atomofiron.regextool.view.custom.bottom_sheet.BottomSheetView

class TasksSheetDelegate(private val activity: RootActivity) {
    private val joystick: Joystick = activity.findViewById(R.id.root_iv_joystick)
    private val bsv: BottomSheetView = activity.findViewById(R.id.root_bsv_tasks)

    fun init() {
        joystick.setOnTouchListener { v, event ->
            log("event ${event.action} ${event.rawY}")
            false
        }
        bsv.setView(RecyclerView(activity))
    }
}