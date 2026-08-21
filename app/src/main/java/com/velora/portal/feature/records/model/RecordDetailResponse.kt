package com.velora.portal.feature.records.model

import android.os.Parcelable
import com.velora.portal.feature.catalog.model.CatalogPlanBean
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class RecordDetailResponse(
    val appOrderInfoDto: RecordItemBean? = null,
    val appOrderRepayDto: RecordItemBean? = null,
    val bankNo: String? = null,
    val interestAmount: BigDecimal? = null,
    val dailyAmount: BigDecimal? = null,
    val interestRateTypeStr: String? = null,
    val actualAmount: BigDecimal? = null,
    val actualRepayAmount: BigDecimal? = null,
    val actualNeedRepayAmount: BigDecimal? = null,
    val totalInstallmentServiceFee: BigDecimal? = null,
    val afterDeductionActualNeedRepayAmount: BigDecimal? = null,
    val penaltyAmount: BigDecimal? = null,
    val repayCode: String? = null,
    val applyDateStr: String? = null,
    val loanDateStr: String? = null,
    val shouldRepayDateStr: String? = null,
    val dayRateStr: String? = null,
    val reliefAmount: BigDecimal? = null,
    val deductionFee: BigDecimal? = null,
    val userCouponName: String? = null,
    val installmentRepaymentPlanDTOList: List<CatalogPlanBean>? = null,
) : Parcelable
