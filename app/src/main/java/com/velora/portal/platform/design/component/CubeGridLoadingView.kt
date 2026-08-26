package com.velora.portal.platform.design.component

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.core.view.animation.PathInterpolatorCompat
import com.velora.portal.R
import kotlin.math.min

/**
 * A local implementation of the 3 x 3 CubeGrid loading animation.
 *
 * Its tile bounds, delays, duration and scale keyframes match
 * Android-SpinKit's CubeGrid without adding the library dependency.
 */
class CubeGridLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.brand_primary)
        style = Paint.Style.FILL
    }
    private val animationsEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ValueAnimator.areAnimatorsEnabled()
    } else {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) != 0f
    }

    private var animationStartedAtMs = 0L
    private var isAttached = false
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = CYCLE_DURATION_MS
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener { invalidate() }
    }

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = dp(DEFAULT_SIZE_DP)
        setMeasuredDimension(
            resolveSize(size, widthMeasureSpec),
            resolveSize(size, heightMeasureSpec),
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
        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom
        if (contentWidth <= 0 || contentHeight <= 0) return

        val gridSize = min(contentWidth, contentHeight)
        val tileWidth = (gridSize * TILE_SIZE_RATIO).toInt()
        if (tileWidth <= 0) return

        val startX = paddingLeft + (contentWidth - gridSize) / 2
        val startY = paddingTop + (contentHeight - gridSize) / 2
        val elapsedMs = SystemClock.uptimeMillis() - animationStartedAtMs
        for (index in 0 until TILE_COUNT) {
            val row = index / GRID_COUNT
            val column = index % GRID_COUNT
            val scale = pulseScale(index, elapsedMs)
            val tileLeft = startX + column * tileWidth
            val tileTop = startY + row * tileWidth
            val inset = tileWidth * (1f - scale) / 2f
            canvas.drawRect(
                tileLeft + inset,
                tileTop + inset,
                tileLeft + tileWidth - inset,
                tileTop + tileWidth - inset,
                paint,
            )
        }
    }

    private fun pulseScale(index: Int, elapsedMs: Long): Float {
        if (!animationsEnabled) return 1f
        val localElapsedMs = elapsedMs - TILE_DELAYS_MS[index]
        if (localElapsedMs < 0L) return 1f
        val phase = (localElapsedMs % CYCLE_DURATION_MS).toFloat() / CYCLE_DURATION_MS
        return when {
            phase < SHRINK_END -> 1f - EASE_IN_OUT.getInterpolation(phase / SHRINK_END)
            phase < GROW_END -> EASE_IN_OUT.getInterpolation((phase - SHRINK_END) / (GROW_END - SHRINK_END))
            else -> 1f
        }
    }

    private fun startAnimationIfNeeded() {
        if (!isAttached || !isShown || windowVisibility != VISIBLE || animator.isRunning) return
        animationStartedAtMs = SystemClock.uptimeMillis()
        if (animationsEnabled) animator.start() else invalidate()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()

    private companion object {
        const val DEFAULT_SIZE_DP = 60
        const val GRID_COUNT = 3
        const val TILE_COUNT = GRID_COUNT * GRID_COUNT
        const val CYCLE_DURATION_MS = 1_300L
        const val TILE_SIZE_RATIO = 0.33f
        const val SHRINK_END = 0.35f
        const val GROW_END = 0.70f

        val TILE_DELAYS_MS = longArrayOf(
            200L, 300L, 400L,
            100L, 200L, 300L,
            0L, 100L, 200L,
        )
        val EASE_IN_OUT = PathInterpolatorCompat.create(0.42f, 0f, 0.58f, 1f)
    }
}
