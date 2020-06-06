package ru.atomofiron.regextool.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.graphics.RectF
import android.text.style.ReplacementSpan

class RoundedBackgroundSpan(
        private val backgroundColor: Int,
        private val textColor: Int,
        private val radius: Float
) : ReplacementSpan() {
    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: FontMetricsInt?): Int {
        return paint.measureText(text.subSequence(start, end).toString()).toInt()
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val width = paint.measureText(text.subSequence(start, end).toString())
        val rect = RectF(x, top.toFloat(), x + width, bottom.toFloat())
        paint.color = backgroundColor
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.color = textColor
        canvas.drawText(text, start, end, x, y.toFloat(), paint)
    }
}