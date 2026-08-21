package com.velora.portal.domain.customer.model

import com.velora.portal.platform.common.data.bean.SelectionOption

data class EmploymentOptionsResponse(
    val relatives: MutableList<SelectionOption>? = null,
    val salaryRange: MutableList<SelectionOption>? = null,
    val otherRelatives: MutableList<SelectionOption>? = null,
    val jobnature: MutableList<SelectionOption>? = null,
    val staffSize: MutableList<SelectionOption>? = null,
    val industry: MutableList<SelectionOption>? = null,
)
