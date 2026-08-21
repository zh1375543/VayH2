package com.velora.portal.platform.design.component

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.velora.portal.platform.design.component.shape.ShapeAttributeReader
import com.velora.portal.platform.design.component.shape.ShapeBackgroundController

/**
 * A LinearLayout that renders the shared ShapeView background attributes.
 */
class StyledLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private val shapeAppearance = ShapeAttributeReader(context).readAppearance(attrs)
    private val backgroundController = ShapeBackgroundController(this)

    init {
        backgroundController.apply(shapeAppearance)
    }
}
