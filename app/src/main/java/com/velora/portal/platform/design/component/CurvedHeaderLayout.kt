package com.velora.portal.platform.design.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.velora.portal.R
import kotlin.math.max

/**
 * A content-sized header whose lower edge forms a width-aware elliptical arc.
 *
 * The layout automatically reserves [contentSpacing] between its content and the start of the
 * arc, so callers can use `wrap_content` without calculating a page-specific header height.
 */
class CurvedHeaderLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val fillPath = Path()
    private val arcDepth: Int
    private val contentSpacing: Int

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CurvedHeaderLayout)
        try {
            fillPaint.color = attributes.getColor(
                R.styleable.CurvedHeaderLayout_curvedHeaderFillColor,
                Color.TRANSPARENT,
            )
            arcDepth = attributes.getDimensionPixelSize(
                R.styleable.CurvedHeaderLayout_curvedHeaderArcDepth,
                DEFAULT_ARC_DEPTH_DP.dp,
            )
            contentSpacing = attributes.getDimensionPixelSize(
                R.styleable.CurvedHeaderLayout_curvedHeaderContentSpacing,
                DEFAULT_CONTENT_SPACING_DP.dp,
            )
        } finally {
            attributes.recycle()
        }

        setWillNotDraw(false)
        setPaddingRelative(
            paddingStart,
            paddingTop,
            paddingEnd,
            paddingBottom + contentSpacing + arcDepth,
        )
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        updateFillPath(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(fillPath, fillPaint)
        super.onDraw(canvas)
    }

    private fun updateFillPath(width: Int, height: Int) {
        val panelWidth = width.toFloat()
        val panelHeight = height.toFloat()
        val resolvedArcDepth = max(0f, arcDepth.toFloat().coerceAtMost(panelHeight))
        val arcStartY = panelHeight - resolvedArcDepth
        val arcControlY = arcStartY + resolvedArcDepth * ELLIPSE_CONTROL_FACTOR

        fillPath.reset()
        fillPath.moveTo(0f, 0f)
        fillPath.lineTo(panelWidth, 0f)
        fillPath.lineTo(panelWidth, arcStartY)
        fillPath.cubicTo(
            panelWidth * RIGHT_CONTROL_X,
            arcControlY,
            panelWidth * LEFT_CONTROL_X,
            arcControlY,
            0f,
            arcStartY,
        )
        fillPath.close()
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density + 0.5f).toInt()

    private companion object {
        const val DEFAULT_ARC_DEPTH_DP = 36
        const val DEFAULT_CONTENT_SPACING_DP = 20
        const val RIGHT_CONTROL_X = 0.78f
        const val LEFT_CONTROL_X = 0.22f

        // A cubic curve reaches the ellipse's lowest point when its control depth is 4/3 of it.
        const val ELLIPSE_CONTROL_FACTOR = 4f / 3f
    }
}
