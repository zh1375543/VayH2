package com.novexa.platform.core.ui.dialog

import android.content.Context
import com.novexa.platform.core.ui.base.BaseSheetDialog
import com.novexa.platform.core.ui.extension.singleClick
import com.novexa.platform.core.common.data.bean.SelectionOption
import com.novexa.platform.databinding.DatePickDialogBinding
import com.novexa.platform.databinding.DialogOptionPickerBinding

fun Context.showOptionPickerDialog(
    defPosition: Int,
    list: List<SelectionOption>?,
    action: (position: Int) -> Unit,
) {
    object : BaseSheetDialog<DialogOptionPickerBinding>(this, DialogOptionPickerBinding::inflate) {
        override fun initView() = with(binding) {
            super.initView()
            wheelView.apply {
                setData(list.orEmpty())
                setDefaultSelected(defPosition)
            }
            tvOk.singleClick {
                dismiss()
                action(wheelView.getSelectedPosition())
            }
        }
    }.show()
}

fun Context.showDatePickerDialog(
    action: (dateStr: String) -> Unit,
) {
    object :
        BaseSheetDialog<DatePickDialogBinding>(this, DatePickDialogBinding::inflate) {
        override fun initView() = with(binding) {
            super.initView()
            root.setOnClickListener { dismiss() }
            tvOk.singleClick {
                dismiss()
                action(dateView.getDateString())
            }
        }
    }.show()
}
