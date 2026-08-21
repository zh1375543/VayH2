package com.velora.portal.core.ui.dialog

import android.content.Context
import com.velora.portal.core.ui.base.BaseSheetDialog
import com.velora.portal.core.ui.extension.singleClick
import com.velora.portal.databinding.TimePickDialogBinding

/** Shows a two-column picker for times in 30-minute increments. */
fun Context.showWorkTimePickerDialog(
    selectedHour: Int,
    selectedMinute: Int,
    action: (hour: Int, minute: Int) -> Unit,
) {
    object : BaseSheetDialog<TimePickDialogBinding>(this, TimePickDialogBinding::inflate) {
        override fun initView() = with(binding) {
            super.initView()
            timeView.setSelectedTime(selectedHour, selectedMinute)
            tvOk.singleClick {
                val hour = timeView.getSelectedHour()
                val minute = timeView.getSelectedMinute()
                dismiss()
                action(hour, minute)
            }
        }
    }.show()
}
