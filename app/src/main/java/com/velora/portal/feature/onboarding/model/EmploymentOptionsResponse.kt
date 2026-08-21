package com.velora.portal.feature.onboarding.model

import com.velora.portal.core.common.data.bean.SelectionOption

data class EmploymentOptionsResponse(
    val relatives: MutableList<SelectionOption>? = null,
    val salaryRange: MutableList<SelectionOption>? = null,
    val otherRelatives: MutableList<SelectionOption>? = null,
    val jobnature: MutableList<SelectionOption>? = null,
    val staffSize: MutableList<SelectionOption>? = null,
    val industry: MutableList<SelectionOption>? = null,
)
