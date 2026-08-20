package com.novexa.platform.core.ui.component

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.novexa.platform.core.ui.component.shape.ShapeAttributeReader
import com.novexa.platform.core.ui.component.shape.ShapeBackgroundController

/** A RecyclerView that renders the shared ShapeView background attributes. */
class StyledRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RecyclerView(context, attrs, defStyleAttr) {

    private val shapeAppearance = ShapeAttributeReader(context).readAppearance(attrs)
    private val backgroundController = ShapeBackgroundController(this)

    init {
        backgroundController.apply(shapeAppearance)
    }
}
