package com.novexa.platform.core.ui.dialog

import android.content.Context
import com.novexa.platform.core.ui.base.BaseSheetDialog
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.databinding.TimePickDialogBinding

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
