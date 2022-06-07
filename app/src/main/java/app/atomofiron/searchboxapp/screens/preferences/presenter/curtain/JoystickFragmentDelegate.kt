package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.TextView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.preference.JoystickComposition
import app.atomofiron.searchboxapp.databinding.CurtainPreferenceJoystickBinding
import app.atomofiron.searchboxapp.injectable.store.util.PreferenceNode
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import lib.atomofiron.android_window_insets_compat.ViewInsetsController

class JoystickFragmentDelegate(
    private val joystickNode: PreferenceNode<JoystickComposition, Int>,
) : CurtainApi.Adapter<CurtainApi.ViewHolder>() {

    private var entity: JoystickComposition = joystickNode.entity

    override fun getHolder(inflater: LayoutInflater, container: ViewGroup, layoutId: Int): CurtainApi.ViewHolder {
        val binding = CurtainPreferenceJoystickBinding.inflate(inflater, container, false)
        binding.init()
        ViewInsetsController.bindPadding(binding.root, top = true, bottom = true)
        return CurtainApi.ViewHolder(binding.root)
    }

    private fun CurtainPreferenceJoystickBinding.init() {
        preferenceJoystickTvTitle.text = entity.text()

        val listener = Listener(preferenceJoystickTvTitle)
        preferenceSbRed.setOnSeekBarChangeListener(listener)
        preferenceSbGreen.setOnSeekBarChangeListener(listener)
        preferenceSbBlue.setOnSeekBarChangeListener(listener)
        preferenceInvForTheme.setOnClickListener(listener)
        preferenceInvHighlight.setOnClickListener(listener)

        preferenceSbRed.progress = entity.red
        preferenceSbGreen.progress = entity.green
        preferenceSbBlue.progress = entity.blue
        preferenceInvForTheme.isChecked = entity.invForDark
        preferenceInvHighlight.isChecked = entity.invGlowing
    }

    private inner class Listener(
        private val tvTitle: TextView,
    ) : SeekBar.OnSeekBarChangeListener, View.OnClickListener {

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            entity = when (seekBar.id) {
                R.id.preference_sb_red -> entity.copy(red = progress)
                R.id.preference_sb_green -> entity.copy(green = progress)
                R.id.preference_sb_blue -> entity.copy(blue = progress)
                else -> throw Exception()
            }
            tvTitle.text = entity.text()
            joystickNode.notify(entity)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

        override fun onStopTrackingTouch(seekBar: SeekBar?) = joystickNode.pushByEntity(entity)

        override fun onClick(view: View) {
            view as CompoundButton
            entity = when (view.id) {
                R.id.preference_inv_for_theme -> entity.copy(invForDark = view.isChecked)
                R.id.preference_inv_highlight -> entity.copy(invGlowing = view.isChecked)
                else -> throw Exception()
            }
            joystickNode.pushByEntity(entity)
        }
    }
}