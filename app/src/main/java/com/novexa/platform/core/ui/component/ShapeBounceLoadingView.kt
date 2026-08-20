package com.novexa.platform.core.ui.component

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.novexa.platform.R
import kotlin.math.min

/**
 * A lightweight geometric loading indicator that alternates between falling and bouncing.
 */
class ShapeBounceLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val shapeColors = intArrayOf(
        ContextCompat.getColor(context, R.color.brand_primary),
        ContextCompat.getColor(context, R.color.brand_secondary),
        ContextCompat.getColor(context, R.color.badge_promotion)
    )
    private val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        alpha = SHADOW_ALPHA
        style = Paint.Style.FILL
    }
    private val trianglePath = Path()
    private val animationsEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ValueAnimator.areAnimatorsEnabled()
    } else {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) != 0f
    }

    private var animationProgress = 0f
    private var currentShape = 0
    private var isAttached = false
    private var shouldAdvanceShape = true

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = CYCLE_DURATION_MS
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener { valueAnimator ->
            val progress = valueAnimator.animatedValue as Float
            if (shouldAdvanceShape && progress >= HALF_CYCLE && animationProgress < HALF_CYCLE) {
                currentShape = (currentShape + 1) % SHAPE_COUNT
            }
            animationProgress = progress
            invalidate()
        }
    }

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = dp(DEFAULT_SIZE_DP)
        setMeasuredDimension(
            resolveSize(size, widthMeasureSpec),
            resolveSize(size, heightMeasureSpec)
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAttached = true
        startAnimationIfNeeded()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        isAttached = false
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) startAnimationIfNeeded() else animator.cancel()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == VISIBLE) startAnimationIfNeeded() else animator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val availableWidth = width.toFloat()
        val availableHeight = height.toFloat()
        if (availableWidth <= 0f || availableHeight <= 0f) return

        val shapeSize = min(availableWidth, availableHeight) * SHAPE_SIZE_RATIO
        val centerX = availableWidth / 2f
        val topY = (availableHeight - shapeSize) * TOP_POSITION_RATIO
        val bottomY = availableHeight - shapeSize - availableHeight * BOTTOM_INSET_RATIO
        val isFalling = animationProgress < HALF_CYCLE
        val phaseProgress = if (isFalling) animationProgress / HALF_CYCLE else {
            (animationProgress - HALF_CYCLE) / HALF_CYCLE
        }
        val easedProgress = if (isFalling) phaseProgress * phaseProgress else {
            1f - (1f - phaseProgress) * (1f - phaseProgress)
        }
        val centerY = if (isFalling) {
            topY + (bottomY - topY) * easedProgress + shapeSize / 2f
        } else {
            bottomY - (bottomY - topY) * easedProgress + shapeSize / 2f
        }
        val shadowScale = if (isFalling) {
            SHADOW_MIN_SCALE + (1f - SHADOW_MIN_SCALE) * easedProgress
        } else {
            1f - (1f - SHADOW_MIN_SCALE) * easedProgress
        }
        val shadowY = bottomY + shapeSize + availableHeight * SHADOW_GAP_RATIO
        val shapeColor = shapeColors[currentShape]
        shapePaint.color = shapeColor
        shadowPaint.color = shapeColor

        canvas.drawOval(
            centerX - shapeSize * SHADOW_WIDTH_RATIO * shadowScale,
            shadowY - shapeSize * SHADOW_HEIGHT_RATIO,
            centerX + shapeSize * SHADOW_WIDTH_RATIO * shadowScale,
            shadowY + shapeSize * SHADOW_HEIGHT_RATIO,
            shadowPaint
        )

        val rotation = if (isFalling) 0f else 180f * easedProgress
        canvas.save()
        canvas.rotate(rotation, centerX, centerY)
        drawShape(canvas, centerX, centerY, shapeSize)
        canvas.restore()
    }

    private fun drawShape(canvas: Canvas, centerX: Float, centerY: Float, size: Float) {
        val half = size / 2f
        when (currentShape) {
            SHAPE_CIRCLE -> canvas.drawCircle(centerX, centerY, half, shapePaint)
            SHAPE_RECTANGLE -> canvas.drawRoundRect(
                centerX - half,
                centerY - half,
                centerX + half,
                centerY + half,
                size * CORNER_RADIUS_RATIO,
                size * CORNER_RADIUS_RATIO,
                shapePaint
            )
            else -> {
                trianglePath.reset()
                trianglePath.moveTo(centerX, centerY - half)
                trianglePath.lineTo(centerX - half, centerY + half)
                trianglePath.lineTo(centerX + half, centerY + half)
                trianglePath.close()
                canvas.drawPath(trianglePath, shapePaint)
            }
        }
    }

    private fun startAnimationIfNeeded() {
        if (!isAttached || !isShown || windowVisibility != VISIBLE || animator.isRunning) return
        shouldAdvanceShape = animationsEnabled
        if (animationsEnabled) animator.start() else invalidate()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()

    private companion object {
        const val DEFAULT_SIZE_DP = 64
        const val CYCLE_DURATION_MS = 1_000L
        const val HALF_CYCLE = 0.5f
        const val SHAPE_COUNT = 3
        const val SHAPE_CIRCLE = 0
        const val SHAPE_RECTANGLE = 1
        const val SHADOW_ALPHA = 72
        const val SHAPE_SIZE_RATIO = 0.31f
        const val TOP_POSITION_RATIO = 0.08f
        const val BOTTOM_INSET_RATIO = 0.22f
        const val SHADOW_GAP_RATIO = 0.06f
        const val SHADOW_MIN_SCALE = 0.2f
        const val SHADOW_WIDTH_RATIO = 0.62f
        const val SHADOW_HEIGHT_RATIO = 0.10f
        const val CORNER_RADIUS_RATIO = 0.14f
    }
}
