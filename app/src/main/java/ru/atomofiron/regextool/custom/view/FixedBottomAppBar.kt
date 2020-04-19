package ru.atomofiron.regextool.custom.view

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS
import com.google.android.material.shape.MaterialShapeUtils
import ru.atomofiron.regextool.R

class FixedBottomAppBar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), AttachedBehavior {
    private val materialShapeDrawable = MaterialShapeDrawable()
    private val mBehavior = Behavior(context)

    init {
        materialShapeDrawable.shadowCompatibilityMode = SHADOW_COMPAT_MODE_ALWAYS
        materialShapeDrawable.paintStyle = Paint.Style.FILL
        materialShapeDrawable.initializeElevationOverlay(context)
        materialShapeDrawable.elevation = resources.getDimension(R.dimen.bottom_bar_elevation)
        background = materialShapeDrawable
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        MaterialShapeUtils.setParentAbsoluteElevation(this, materialShapeDrawable)
        (parent as ViewGroup).clipChildren = false
    }

    override fun getBehavior(): Behavior = mBehavior

    class Behavior @JvmOverloads constructor(
            context: Context, attrs: AttributeSet? = null
    ) : FixedHideBottomViewOnScrollBehavior<FixedBottomAppBar>(context, attrs)
}