package com.velora.portal.core.ui.component

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.velora.portal.core.common.data.bean.SelectionOption

/** Two-column wheel for selecting a time in half-hour increments. */
class WheelTimeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    private val hourWheel = WheelView(context)
    private val minuteWheel = WheelView(context)
    private var selectedHour = 0
    private var selectedMinute = 0

    init {
        orientation = HORIZONTAL
        addView(hourWheel, LayoutParams(0, LayoutParams.MATCH_PARENT, 1f))
        addView(minuteWheel, LayoutParams(0, LayoutParams.MATCH_PARENT, 1f))

        hourWheel.setOnSelectListener { _, option ->
            selectedHour = option.info.toIntOrNull() ?: selectedHour
            if (selectedHour == LAST_HOUR) selectedMinute = 0
            refreshMinutes()
        }
        minuteWheel.setOnSelectListener { _, option ->
            selectedMinute = option.info.toIntOrNull() ?: selectedMinute
        }
        setSelectedTime(0, 0)
    }

    fun setSelectedTime(hour: Int, minute: Int) {
        selectedHour = hour.coerceIn(FIRST_HOUR, LAST_HOUR)
        selectedMinute = minute.takeIf { it in MINUTES } ?: 0
        if (selectedHour == LAST_HOUR) selectedMinute = 0
        hourWheel.setData(
            (FIRST_HOUR..LAST_HOUR).map { value -> SelectionOption(value.toString().padStart(2, '0')) },
            selectedHour,
        )
        refreshMinutes()
    }

    fun getSelectedHour(): Int = selectedHour

    fun getSelectedMinute(): Int = selectedMinute

    private fun refreshMinutes() {
        val availableMinutes = if (selectedHour == LAST_HOUR) listOf(0) else MINUTES
        if (selectedMinute !in availableMinutes) selectedMinute = 0
        minuteWheel.setData(
            availableMinutes.map { value -> SelectionOption(value.toString().padStart(2, '0')) },
            availableMinutes.indexOf(selectedMinute).coerceAtLeast(0),
        )
    }

    private companion object {
        const val FIRST_HOUR = 0
        const val LAST_HOUR = 24
        val MINUTES = listOf(0, 30)
    }
}
