package com.velora.portal.platform.design.component

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.velora.portal.platform.design.component.shape.ShapeAttributeReader
import com.velora.portal.platform.design.component.shape.ShapeBackgroundController

/** A ConstraintLayout that renders the shared ShapeView background attributes. */
class StyledConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val shapeAppearance = ShapeAttributeReader(context).readAppearance(attrs)
    private val backgroundController = ShapeBackgroundController(this)

    init {
        backgroundController.apply(shapeAppearance)
    }
}
