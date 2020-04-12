package ru.atomofiron.regextool.screens.preferences.fragment

import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.TextView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.model.JoystickComposition
import ru.atomofiron.regextool.view.custom.bottom_sheet.BottomSheetDelegate

class JoystickFragmentDelegate(
        private var entity: JoystickComposition,
        private val joystickPreferenceOutput: JoystickPreferenceOutput
) : BottomSheetDelegate(R.layout.sheet_preference_joystick) {

    private val tvTitle: TextView get() = bottomSheetView.findViewById(R.id.preference_joystick_tv_title)
    private val sbRed: SeekBar get() = bottomSheetView.findViewById(R.id.preference_sb_red)
    private val sbGreen: SeekBar get() = bottomSheetView.findViewById(R.id.preference_sb_green)
    private val sbBlue: SeekBar get() = bottomSheetView.findViewById(R.id.preference_sb_blue)
    private val cbInvForDark: CheckBox get() = bottomSheetView.findViewById(R.id.preference_inv_for_theme)
    private val cbInvHighlight: CheckBox get() = bottomSheetView.findViewById(R.id.preference_inv_highlight)

    private val listener = Listener()

    public override fun show() = super.show()

    override fun onViewReady() {
        tvTitle.text = entity.text()

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
            tvTitle.text = entity.text()
            joystickPreferenceOutput.notify(entity)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

        override fun onStopTrackingTouch(seekBar: SeekBar?) = joystickPreferenceOutput.push(entity)

        override fun onClick(view: View) {
            view as CompoundButton
            entity = when (view.id) {
                R.id.preference_inv_for_theme -> entity.copy(invForDark = view.isChecked)
                R.id.preference_inv_highlight -> entity.copy(invGlowing = view.isChecked)
                else -> throw Exception()
            }
            joystickPreferenceOutput.push(entity)
        }
    }

    interface JoystickPreferenceOutput {
        fun notify(composition: JoystickComposition)
        fun push(composition: JoystickComposition)
    }
}