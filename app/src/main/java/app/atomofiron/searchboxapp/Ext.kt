package app.atomofiron.searchboxapp

import android.view.View
import androidx.fragment.app.Fragment

val Fragment.anchorView: View get() = requireActivity().findViewById(R.id.joystick)
