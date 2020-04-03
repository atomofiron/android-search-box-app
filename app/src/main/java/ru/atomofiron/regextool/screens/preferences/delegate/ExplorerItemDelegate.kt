package ru.atomofiron.regextool.screens.preferences.delegate

import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.iss.service.explorer.model.MutableXFile
import ru.atomofiron.regextool.screens.explorer.adapter.ExplorerHolder
import ru.atomofiron.regextool.screens.preferences.PreferenceViewModel
import ru.atomofiron.regextool.utils.Const
import ru.atomofiron.regextool.view.custom.bottom_sheet.BottomSheetDelegate

class ExplorerItemDelegate(
        private val viewModel: PreferenceViewModel
) : BottomSheetDelegate(R.layout.layout_explorer_item) {
    private val dir = MutableXFile("-rwxrwxrwx", "atomofiron", "everybody", "4KB", "19-01-2038",
            "03:14", "Android", "", isDirectory = true, absolutePath = "/sdcard/Android", root = 0)

    private val explorerItem: View get() = bottomSheetView.findViewById(R.id.preference_explorer_item)
    private val ivIcon: ImageView get() = bottomSheetView.findViewById(R.id.item_explorer_iv_icon)
    private val cpFlag: CheckBox get() = bottomSheetView.findViewById(R.id.item_explorer_cb)
    private val tvDescription: TextView get() = bottomSheetView.findViewById(R.id.item_explorer_tv_description)
    private val tvDateTime: TextView get() = bottomSheetView.findViewById(R.id.item_explorer_tv_date)
    private val tvSize: TextView get() = bottomSheetView.findViewById(R.id.item_explorer_tv_size)

    private val cbAccess: CheckBox get() = bottomSheetView.findViewById(R.id.preference_access)
    private val cbOwner: CheckBox get() = bottomSheetView.findViewById(R.id.preference_owner)
    private val cbGroup: CheckBox get() = bottomSheetView.findViewById(R.id.preference_group)
    private val cbDate: CheckBox get() = bottomSheetView.findViewById(R.id.preference_date)
    private val cbTime: CheckBox get() = bottomSheetView.findViewById(R.id.preference_time)

    private val listener = Listener()

    private var bitsAccess = 0b10000
    private var bitsOwner = 0b01000
    private var bitsGroup = 0b00100
    private var bitsDate = 0b00010
    private var bitsTime = 0b00001

    private var visibleAccess = true
    private var visibleOwner = true
    private var visibleGroup = true
    private var visibleDate = true
    private var visibleTime = true

    init {
        val state = viewModel.explorerItemState
        visibleAccess = state and bitsAccess == bitsAccess
        visibleOwner = state and bitsOwner == bitsOwner
        visibleGroup = state and bitsGroup == bitsGroup
        visibleDate = state and bitsDate == bitsDate
        visibleTime = state and bitsTime == bitsTime
    }

    override fun onViewReady() {
        cbAccess.isChecked = visibleAccess
        cbOwner.isChecked = visibleOwner
        cbGroup.isChecked = visibleGroup
        cbDate.isChecked = visibleDate
        cbTime.isChecked = visibleTime

        cbAccess.setOnClickListener(listener)
        cbOwner.setOnClickListener(listener)
        cbGroup.setOnClickListener(listener)
        cbDate.setOnClickListener(listener)
        cbTime.setOnClickListener(listener)

        ExplorerHolder(explorerItem).bind(dir, 0)

        ivIcon.alpha = 1f
        explorerItem.background = null
        explorerItem.isFocusable = false
        explorerItem.isClickable = false
        cpFlag.isFocusable = false
        cpFlag.isClickable = false
        cpFlag.isChecked = true
        tvSize.text = "4KB"

        updatePreview()
    }

    fun updatePreview() {
        val string = StringBuilder()
        if (visibleAccess) {
            string.append(dir.access).append(" ")
        }
        if (visibleOwner) {
            string.append(dir.owner).append(" ")
        }
        if (visibleGroup) {
            string.append(dir.group).append(" ")
        }
        tvDescription.text = string.toString()
        string.clear()
        if (visibleDate) {
            string.append(dir.date)
        }
        if (visibleTime) {
            string.append(" ").append(dir.time)
        }
        tvDateTime.text = string.toString()
    }
    
    private fun pushState() {
        var state = 0
        if (visibleAccess) {
            state += bitsAccess
        }
        if (visibleOwner) {
            state += bitsOwner
        }
        if (visibleGroup) {
            state += bitsGroup
        }
        if (visibleDate) {
            state += bitsDate
        }
        if (visibleTime) {
            state += bitsTime
        }
        viewModel.onPreferenceUpdate(Const.PREF_EXPLORER_ITEM, state)
    }

    private inner class Listener : View.OnClickListener {
        override fun onClick(view: View) {
            view as CompoundButton
            val isChecked = view.isChecked
            when (view.id) {
                R.id.preference_access -> visibleAccess = isChecked
                R.id.preference_owner -> visibleOwner = isChecked
                R.id.preference_group -> visibleGroup = isChecked
                R.id.preference_date -> visibleDate = isChecked
                R.id.preference_time -> visibleTime = isChecked
                else -> throw Exception()
            }
            updatePreview()
            pushState()
        }
    }
}