package app.atomofiron.searchboxapp.screens.preferences.fragment

import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.bottom_sheet.BottomSheetDelegate
import app.atomofiron.searchboxapp.model.explorer.MutableXFile
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.adapter.ExplorerHolder
import app.atomofiron.searchboxapp.utils.Const

class ExplorerItemFragmentDelegate(
    private var composition: ExplorerItemComposition,
    private val output: PreferenceUpdateOutput
) : BottomSheetDelegate(R.layout.sheet_preference_explorer_item) {
    private val dir = MutableXFile("drwxrwx---", "atomofiron", "everybody", "4KB", "19-01-2038",
            "03:14", "Android", "", isDirectory = true, absolutePath = "/sdcard/Android")

    private val itemView: View get() = bottomSheetView.findViewById(R.id.preference_explorer_item)
    private val ivIcon: ImageView get() = itemView.findViewById(R.id.item_explorer_iv_icon)
    private val tvSize: TextView get() = itemView.findViewById(R.id.item_explorer_tv_size)

    private val cbAccess: CheckBox get() = bottomSheetView.findViewById(R.id.preference_access)
    private val cbOwner: CheckBox get() = bottomSheetView.findViewById(R.id.preference_owner)
    private val cbGroup: CheckBox get() = bottomSheetView.findViewById(R.id.preference_group)
    private val cbDate: CheckBox get() = bottomSheetView.findViewById(R.id.preference_date)
    private val cbTime: CheckBox get() = bottomSheetView.findViewById(R.id.preference_time)
    private val cbSize: CheckBox get() = bottomSheetView.findViewById(R.id.preference_size)
    private val cbBox: CheckBox get() = bottomSheetView.findViewById(R.id.preference_box)
    private val cbBg: CheckBox get() = bottomSheetView.findViewById(R.id.preference_bg)

    private val onClickListener = Listener()
    private lateinit var holder: ExplorerHolder

    public override fun show() = super.show()

    override fun onViewReady() {
        cbAccess.isChecked = composition.visibleAccess
        cbOwner.isChecked = composition.visibleOwner
        cbGroup.isChecked = composition.visibleGroup
        cbDate.isChecked = composition.visibleDate
        cbTime.isChecked = composition.visibleTime
        cbSize.isChecked = composition.visibleSize
        cbBox.isChecked = composition.visibleBox
        cbBg.isChecked = composition.visibleBg

        cbAccess.setOnClickListener(onClickListener)
        cbOwner.setOnClickListener(onClickListener)
        cbGroup.setOnClickListener(onClickListener)
        cbDate.setOnClickListener(onClickListener)
        cbTime.setOnClickListener(onClickListener)
        cbSize.setOnClickListener(onClickListener)
        cbBox.setOnClickListener(onClickListener)
        cbBg.setOnClickListener(onClickListener)

        holder = ExplorerHolder(itemView)
        holder.bind(dir)
        holder.bindComposition(composition)
        holder.disableClicks()

        ivIcon.alpha = 1f
        tvSize.text = dir.size

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
            output.onPreferenceUpdate(Const.PREF_EXPLORER_ITEM, composition.flags)
        }
    }

    private fun bindBackground() = holder.setGreyBackgroundColor(composition.visibleBg)
}