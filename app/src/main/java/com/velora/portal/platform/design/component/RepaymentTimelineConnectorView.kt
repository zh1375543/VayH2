package com.velora.portal.platform.design.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import com.velora.portal.R

/** Draws the vertical connector between consecutive repayment schedule nodes. */
class RepaymentTimelineConnectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val connectorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.SQUARE
    }

    private var connectorColor = ContextCompat.getColor(context, R.color.text_tertiary)
    private var connectorWidth = resources.displayMetrics.density

    init {
        context.obtainStyledAttributes(attrs, R.styleable.RepaymentTimelineConnectorView).use {
            connectorColor = it.getColor(
                R.styleable.RepaymentTimelineConnectorView_connectorColor,
                connectorColor,
            )
            connectorWidth = it.getDimension(
                R.styleable.RepaymentTimelineConnectorView_connectorWidth,
                connectorWidth,
            )
        }
        connectorPaint.color = connectorColor
        connectorPaint.strokeWidth = connectorWidth
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        canvas.drawLine(centerX, 0f, centerX, height.toFloat(), connectorPaint)
    }
}
