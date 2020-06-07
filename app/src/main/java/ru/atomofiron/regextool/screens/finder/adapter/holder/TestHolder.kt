package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import app.atomofiron.common.util.findColorByAttr
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem
import ru.atomofiron.regextool.utils.RoundedBackgroundSpan
import java.util.regex.Pattern

class TestHolder(parent: ViewGroup, id: Int) : CardViewHolder(parent, id), TextWatcher {
    private val editText: EditText
    private val span = RoundedBackgroundSpan(
            context.findColorByAttr(R.attr.colorAccent),
            ContextCompat.getColor(context, R.color.white),
            context.resources.getDimension(R.dimen.background_span_corner_radius)
    )

    init {
        itemView.isFocusable = false
        itemView.isClickable = false

        editText = itemView.findViewById(R.id.layout_et_test)
        editText.addTextChangedListener(this)
    }

    override fun onBind(item: FinderStateItem, position: Int) = test(item)

    private fun test(item: FinderStateItem) {
        editText.text.getSpans(0, editText.text.length, RoundedBackgroundSpan::class.java).forEach {
            editText.text.removeSpan(it)
        }
        item as FinderStateItem.TestItem

        when {
            item.searchQuery.isEmpty() -> Unit
            item.useRegex -> testSearchWithRegexp(item)
            else -> testSearch(item)
        }
    }

    private fun testSearch(item: FinderStateItem.TestItem) {
        var offset = 0
        val length = item.searchQuery.length
        var index = editText.text.indexOf(item.searchQuery, offset, item.ignoreCase)
        while (index != -1) {
            editText.text.setSpan(span, index, index + length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            offset = index + length
            index = editText.text.indexOf(item.searchQuery, offset, item.ignoreCase)
        }
    }

    private fun testSearchWithRegexp(item: FinderStateItem.TestItem) {
        var flags = 0
        if (item.ignoreCase) {
            flags = flags or Pattern.CASE_INSENSITIVE
        }
        val pattern: Pattern
        try {
            pattern = Pattern.compile(item.searchQuery, flags)

            var offset = 0
            val lines = editText.text.toString().split('\n')
            for (line in lines) {
                val matcher = pattern.matcher(line)

                while (matcher.find() && matcher.start() != matcher.end()) {
                    editText.text.setSpan(span, offset + matcher.start(), offset + matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                offset += line.length.inc()
            }
        } catch (e: Exception) {
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(s: Editable?) = test(item)
}