package app.atomofiron.searchboxapp.screens.viewer.recycler

import android.text.Spannable
import android.text.SpannableString
import android.widget.TextView
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.style.EntireLineSpan
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch
import app.atomofiron.searchboxapp.custom.view.style.RoundedBackgroundSpan

class TextViewerHolder(private val textView: TextView) : GeneralHolder<TextLine>(textView) {
    private val spanPart: RoundedBackgroundSpan
        get() = RoundedBackgroundSpan(
            context.findColorByAttr(R.attr.colorSecondary),
            context.findColorByAttr(R.attr.colorOnSecondary),
            context.resources.getDimension(R.dimen.background_span_corner_radius)
    )

    private val spanPartFocus: RoundedBackgroundSpan
        get() = RoundedBackgroundSpan(
            context.findColorByAttr(R.attr.colorTertiary),
            context.findColorByAttr(R.attr.colorOnTertiary),
            context.resources.getDimension(R.dimen.background_span_corner_radius)
    )

    private val spanLine: EntireLineSpan
        get() = EntireLineSpan(
            context.findColorByAttr(R.attr.colorSecondary),
            context.findColorByAttr(R.attr.colorOnSecondary),
            context.resources.getDimension(R.dimen.background_span_corner_radius)
    )

    private val spanLineFocus: EntireLineSpan
        get() = EntireLineSpan(
            context.findColorByAttr(R.attr.colorTertiary),
            context.findColorByAttr(R.attr.colorOnTertiary),
            context.resources.getDimension(R.dimen.background_span_corner_radius)
    )

    override fun onBind(item: TextLine, position: Int) = Unit

    fun onBind(item: TextLine, matches: List<TextLineMatch>?, indexFocus: Int?) {
        when {
            matches == null || matches.isEmpty() -> textView.text = item.text
            else -> {
                val spannable = SpannableString(item.text)
                matches.forEachIndexed { index, match ->
                    val forTheEntireLine = match.start == 0 && match.end == item.text.length
                    val span: Any = when {
                        forTheEntireLine && index == indexFocus -> spanLineFocus
                        forTheEntireLine -> spanLine
                        index == indexFocus -> spanPartFocus
                        else -> spanPart
                    }
                    spannable.setSpan(span, match.start, match.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                textView.text = spannable
            }
        }
    }
}