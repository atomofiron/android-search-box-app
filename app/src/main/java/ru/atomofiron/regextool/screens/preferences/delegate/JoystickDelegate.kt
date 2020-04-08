package ru.atomofiron.regextool.screens.preferences.delegate

import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.SeekBar
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.injectable.store.util.PreferenceNode
import ru.atomofiron.regextool.model.JoystickComposition
import ru.atomofiron.regextool.view.custom.bottom_sheet.BottomSheetDelegate

class JoystickDelegate(
        private val escColorNode: PreferenceNode<JoystickComposition, Int>
) : BottomSheetDelegate(R.layout.sheet_esc_color) {

    private val sbRed: SeekBar get() = bottomSheetView.findViewById(R.id.preference_sb_red)
    private val sbGreen: SeekBar get() = bottomSheetView.findViewById(R.id.preference_sb_green)
    private val sbBlue: SeekBar get() = bottomSheetView.findViewById(R.id.preference_sb_blue)
    private val cbInvForDark: CheckBox get() = bottomSheetView.findViewById(R.id.preference_esc_inv_for_theme)
    private val cbInvHighlight: CheckBox get() = bottomSheetView.findViewById(R.id.preference_esc_inv_highlight)

    private val listener = Listener()
    private var entity = escColorNode.entity

    public override fun show() = super.show()

    override fun onViewReady() {
        sbRed.setOnSeekBarChangeListener(listener)
        sbGreen.setOnSeekBarChangeListener(listener)
        sbBlue.setOnSeekBarChangeListener(listener)
        cbInvForDark.setOnClickListener(listener)
        cbInvHighlight.setOnClickListener(listener)

        sbRed.progress = entity.red
        sbGreen.progress = entity.green
        sbBlue.progress = entity.blue
        cbInvForDark.isChecked = entity.invForDark
        cbInvHighlight.isChecked = entity.invGlowing
    }

    private inner class Listener : SeekBar.OnSeekBarChangeListener, View.OnClickListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            entity = when (seekBar.id) {
                R.id.preference_sb_red -> entity.copy(red = progress)
                R.id.preference_sb_green -> entity.copy(green = progress)
                R.id.preference_sb_blue -> entity.copy(blue = progress)
                else -> throw Exception()
            }
            escColorNode.notify(entity)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

        override fun onStopTrackingTouch(seekBar: SeekBar?) = escColorNode.push(entity)

        override fun onClick(view: View) {
            view as CompoundButton
            entity = when (view.id) {
                R.id.preference_esc_inv_for_theme -> entity.copy(invForDark = view.isChecked)
                R.id.preference_esc_inv_highlight -> entity.copy(invGlowing = view.isChecked)
                else -> throw Exception()
            }
            escColorNode.push(entity)
        }
    }
}