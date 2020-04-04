package ru.atomofiron.regextool.screens.preferences.delegate

import android.graphics.Color
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
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
    private val dir = MutableXFile("drwxrwx---", "atomofiron", "everybody", "4KB", "19-01-2038",
            "03:14", "Android", "", isDirectory = true, absolutePath = "/sdcard/Android", root = 0)

    private val explorerItem: View get() = bottomSheetView.findViewById(R.id.preference_explorer_item)
    private val ivIcon: ImageView get() = bottomSheetView.findViewById(R.id.item_explorer_iv_icon)
    private val cbCheckBox: CheckBox get() = bottomSheetView.findViewById(R.id.item_explorer_cb)
    private val tvSize: TextView get() = bottomSheetView.findViewById(R.id.item_explorer_tv_size)

    private val cbAccess: CheckBox get() = bottomSheetView.findViewById(R.id.preference_access)
    private val cbOwner: CheckBox get() = bottomSheetView.findViewById(R.id.preference_owner)
    private val cbGroup: CheckBox get() = bottomSheetView.findViewById(R.id.preference_group)
    private val cbDate: CheckBox get() = bottomSheetView.findViewById(R.id.preference_date)
    private val cbTime: CheckBox get() = bottomSheetView.findViewById(R.id.preference_time)
    private val cbSize: CheckBox get() = bottomSheetView.findViewById(R.id.preference_size)
    private val cbBox: CheckBox get() = bottomSheetView.findViewById(R.id.preference_box)
    private val cbBg: CheckBox get() = bottomSheetView.findViewById(R.id.preference_bg)

    private val listener = Listener()
    private lateinit var holder: ExplorerHolder
    private var backgroundColor: Int = 0

    private var composition: ExplorerItemComposition = viewModel.explorerItemState

    override fun onViewReady() {
        cbAccess.isChecked = composition.visibleAccess
        cbOwner.isChecked = composition.visibleOwner
        cbGroup.isChecked = composition.visibleGroup
        cbDate.isChecked = composition.visibleDate
        cbTime.isChecked = composition.visibleTime
        cbSize.isChecked = composition.visibleSize
        cbBox.isChecked = composition.visibleBox
        cbBg.isChecked = composition.visibleBg

        cbAccess.setOnClickListener(listener)
        cbOwner.setOnClickListener(listener)
        cbGroup.setOnClickListener(listener)
        cbDate.setOnClickListener(listener)
        cbTime.setOnClickListener(listener)
        cbSize.setOnClickListener(listener)
        cbBox.setOnClickListener(listener)
        cbBg.setOnClickListener(listener)

        holder = ExplorerHolder(explorerItem)
        holder.bind(dir, 0)
        holder.bindComposition(composition)

        ivIcon.alpha = 1f
        explorerItem.background = null
        explorerItem.isFocusable = false
        explorerItem.isClickable = false
        tvSize.text = dir.size

        backgroundColor = ContextCompat.getColor(explorerItem.context, R.color.item_explorer_background)
        bindBackground()
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
                R.id.preference_size -> composition.copy(visibleSize = isChecked)
                R.id.preference_box -> composition.copy(visibleBox = isChecked)
                R.id.preference_bg -> composition.copy(visibleBg = isChecked)
                else -> throw Exception()
            }
            holder.bindComposition(composition)
            bindBackground()
            viewModel.onPreferenceUpdate(Const.PREF_EXPLORER_ITEM, composition.flags)
        }
    }

    private fun bindBackground() {
        val color = when (composition.visibleBg) {
            true -> backgroundColor
            else -> Color.TRANSPARENT
        }
        holder.itemView.setBackgroundColor(color)
    }
}