package com.velora.portal.core.ui.dialog

import android.content.Context
import com.velora.portal.core.ui.base.BaseSheetDialog
import com.velora.portal.core.ui.extension.singleClick
import com.velora.portal.core.common.data.bean.SelectionOption
import com.velora.portal.databinding.DatePickDialogBinding
import com.velora.portal.databinding.DialogOptionPickerBinding

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
