package app.atomofiron.searchboxapp.screens.explorer.presenter.curtain

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.databinding.CurtainExplorerCreateBinding
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerCurtainMenuDelegate
import lib.atomofiron.android_window_insets_compat.applyPaddingInsets

class CreateDelegate(
    private val output: ExplorerCurtainMenuDelegate,
) {

    fun getView(dir: XFile, inflater: LayoutInflater, container: ViewGroup): View {
        val dirFiles = dir.children?.map { it.name } ?: listOf()
        val binding = CurtainExplorerCreateBinding.inflate(inflater, container, false)
        binding.init(dir, dirFiles)
        return binding.root
    }

    private fun CurtainExplorerCreateBinding.init(dir: XFile, dirFiles: List<String>) {
        root.applyPaddingInsets(vertical = true)
        root.requestApplyInsets()
        explorerCreateEt.text?.clear()
        explorerCreateEt.addTextChangedListener(ButtonState(dirFiles, explorerCreateBtn))
        explorerCreateBtn.setOnClickListener(ButtonClick(dir, explorerCreateEt, explorerCreateRg))
        explorerCreateBtn.isEnabled = false
    }

    private inner class ButtonClick(
        private val dir: XFile,
        private val editText: EditText,
        private val radioGroup: RadioGroup,
    ) : View.OnClickListener {
        override fun onClick(v: View?) {
            val directory = when (radioGroup.checkedRadioButtonId) {
                R.id.explorer_create_dir -> true
                R.id.explorer_create_file -> false
                else -> throw Exception()
            }
            output.onCreateConfirm(dir, editText.text.toString(), directory)
        }
    }

    private inner class ButtonState(
        private val dirFiles: List<String>,
        private val button: Button,
    ) : TextWatcher {
        override fun afterTextChanged(s: Editable?) = Unit
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(sequence: CharSequence, start: Int, before: Int, count: Int) {
            val allow = sequence.isNotEmpty() && !dirFiles.contains(sequence.toString())
            button.isEnabled = allow
        }
    }
}