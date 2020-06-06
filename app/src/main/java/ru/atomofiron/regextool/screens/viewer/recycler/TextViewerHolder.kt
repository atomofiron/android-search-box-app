package ru.atomofiron.regextool.screens.viewer.recycler

import android.text.Spannable
import android.text.SpannableString
import android.widget.TextView
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.common.util.findColorByAttr
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.model.textviewer.TextLine
import ru.atomofiron.regextool.utils.RoundedBackgroundSpan

class TextViewerHolder(private val textView: TextView) : GeneralHolder<TextLine>(textView) {
    private val span = RoundedBackgroundSpan(
            context.findColorByAttr(R.attr.colorAccent),
            context.findColorByAttr(R.attr.colorNegative),
            context.resources.getDimension(R.dimen.background_span_corner_radius)
    )

    override fun onBind(item: TextLine, position: Int) {
        when (val matches = item.matches) {
            null -> textView.text = item.text
            else -> {
                val spannable = SpannableString(item.text)
                for (match in matches) {
                    spannable.setSpan(span, match.start, match.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                textView.text = spannable
            }
        }
    }
}