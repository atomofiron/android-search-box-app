package ru.atomofiron.regextool.screens.preferences.delegate

import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.iss.service.explorer.model.MutableXFile
import ru.atomofiron.regextool.model.ExplorerItemComposition
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

    private var composition: ExplorerItemComposition = viewModel.explorerItemState

    override fun onViewReady() {
        cbAccess.isChecked = composition.visibleAccess
        cbOwner.isChecked = composition.visibleOwner
        cbGroup.isChecked = composition.visibleGroup
        cbDate.isChecked = composition.visibleDate
        cbTime.isChecked = composition.visibleTime

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
        if (composition.visibleAccess) {
            string.append(dir.access).append(" ")
        }
        if (composition.visibleOwner) {
            string.append(dir.owner).append(" ")
        }
        if (composition.visibleGroup) {
            string.append(dir.group)
        }
        tvDescription.text = string.toString()
        string.clear()
        if (composition.visibleDate) {
            string.append(dir.date)
        }
        if (composition.visibleTime) {
            string.append(" ").append(dir.time)
        }
        tvDateTime.text = string.toString()
    }

    private inner class Listener : View.OnClickListener {
        override fun onClick(view: View) {
            view as CompoundButton
            val isChecked = view.isChecked
            composition = when (view.id) {
                R.id.preference_access -> composition.copy(visibleAccess = isChecked)
                R.id.preference_owner -> composition.copy(visibleOwner = isChecked)
                R.id.preference_group -> composition.copy(visibleGroup = isChecked)
                R.id.preference_date -> composition.copy(visibleDate = isChecked)
                R.id.preference_time -> composition.copy(visibleTime = isChecked)
                else -> throw Exception()
            }
            updatePreview()
            viewModel.onPreferenceUpdate(Const.PREF_EXPLORER_ITEM, composition.flags)
        }
    }
}