package ru.atomofiron.regextool.screens.viewer.recycler

import android.text.Spannable
import android.text.SpannableString
import android.widget.TextView
import androidx.core.content.ContextCompat
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.common.util.findColorByAttr
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.model.textviewer.TextLine
import ru.atomofiron.regextool.utils.RoundedBackgroundSpan

class TextViewerHolder(private val textView: TextView) : GeneralHolder<TextLine>(textView) {
    private val span = RoundedBackgroundSpan(
            context.findColorByAttr(R.attr.colorAccent),
            ContextCompat.getColor(context, R.color.white),
            context.resources.getDimension(R.dimen.background_span_corner_radius)
    )

    override fun onBind(item: TextLine, position: Int) {
    }

    fun onBind(item: TextLine, matches: List<TextLine.Match>?) {
        when (matches) {
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